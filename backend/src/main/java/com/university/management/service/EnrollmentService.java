package com.university.management.service;

import com.university.management.dto.EnrollmentDtos.CreateEnrollmentRequest;
import com.university.management.dto.EnrollmentDtos.EnrollmentResponse;
import com.university.management.entity.Course;
import com.university.management.entity.Enrollment;
import com.university.management.entity.EnrollmentStatus;
import com.university.management.entity.Role;
import com.university.management.entity.Student;
import com.university.management.entity.UserEntity;
import com.university.management.exception.BusinessException;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.CourseRepository;
import com.university.management.repository.EnrollmentRepository;
import com.university.management.repository.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public List<EnrollmentResponse> listEnrollments(UserEntity currentUser) {
        List<Enrollment> enrollments = switch (currentUser.getRole()) {
            case ADMIN -> enrollmentRepository.findAll();
            case TEACHER -> enrollmentRepository.findByCourse_Teacher_User_Id(currentUser.getId());
            case STUDENT -> enrollmentRepository.findByStudent_User_Id(currentUser.getId());
        };
        return enrollments.stream().map(this::toResponse).toList();
    }

    @Transactional
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, UserEntity currentUser) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(student.getUser().getId())) {
            throw new AccessDeniedException("Students can only enroll themselves");
        }

        if (enrollmentRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new ConflictException("Student is already enrolled in this course");
        }

        long count = enrollmentRepository.countByCourse(course);
        if (count >= course.getMaxStudents()) {
            throw new BusinessException("Course is full");
        }

        Enrollment enrollment = new Enrollment(student, course);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void withdrawEnrollment(Long id, UserEntity currentUser) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(enrollment.getStudent().getUser().getId())) {
            throw new AccessDeniedException("Students can only withdraw their own enrollment");
        }

        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCourseCode(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt() == null ? null : enrollment.getEnrolledAt().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
