package com.university.management.service;

import com.university.management.dto.CourseDtos.CourseResponse;
import com.university.management.dto.CourseDtos.CreateCourseRequest;
import com.university.management.dto.CourseDtos.UpdateCourseRequest;
import com.university.management.entity.Course;
import com.university.management.entity.Role;
import com.university.management.entity.Teacher;
import com.university.management.entity.UserEntity;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.CourseRepository;
import com.university.management.repository.TeacherRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public CourseService(CourseRepository courseRepository, TeacherRepository teacherRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<CourseResponse> listCourses(UserEntity currentUser) {
        List<Course> courses;
        if (currentUser.getRole() == Role.TEACHER) {
            Teacher teacher = teacherRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));
            courses = courseRepository.findByTeacher(teacher);
        } else {
            courses = courseRepository.findAll();
        }
        return courses.stream().map(this::toResponse).toList();
    }

    public CourseResponse getCourse(Long id, UserEntity currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        if (currentUser.getRole() == Role.TEACHER
                && (course.getTeacher() == null || !currentUser.getId().equals(course.getTeacher().getUser().getId()))) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        return toResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, UserEntity currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can create courses");
        }
        if (courseRepository.existsByCourseCode(request.courseCode())) {
            throw new ConflictException("Course code already exists");
        }

        Teacher teacher = null;
        if (request.teacherId() != null) {
            teacher = teacherRepository.findById(request.teacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + request.teacherId()));
        }

        Course course = new Course(request.courseCode(), request.title(), request.description(), request.credits(), teacher,
                request.maxStudents() == null ? 40 : request.maxStudents());
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request, UserEntity currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (currentUser.getRole() != Role.ADMIN && (course.getTeacher() == null || !currentUser.getId().equals(course.getTeacher().getUser().getId()))) {
            throw new AccessDeniedException("You can only update courses assigned to you");
        }

        if (request.courseCode() != null && !request.courseCode().isBlank()) {
            if (!request.courseCode().equals(course.getCourseCode()) && courseRepository.existsByCourseCode(request.courseCode())) {
                throw new ConflictException("Course code already exists");
            }
            course.setCourseCode(request.courseCode());
        }
        if (request.title() != null && !request.title().isBlank()) {
            course.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            course.setDescription(request.description());
        }
        if (request.credits() != null) {
            course.setCredits(request.credits());
        }
        if (request.teacherId() != null) {
            if (currentUser.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Only administrators can reassign a course");
            }
            Teacher teacher = teacherRepository.findById(request.teacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
            course.setTeacher(teacher);
        }
        if (request.maxStudents() != null) {
            course.setMaxStudents(request.maxStudents());
        }

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id, UserEntity currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can delete courses");
        }
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        courseRepository.delete(course);
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                course.getDescription(),
                course.getCredits(),
                course.getTeacher() == null ? null : course.getTeacher().getId(),
                course.getTeacher() == null ? null : course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName(),
                course.getMaxStudents()
        );
    }
}
