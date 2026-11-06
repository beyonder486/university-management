package com.university.management.service;

import com.university.management.repository.CourseRepository;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.TeacherRepository;
import com.university.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public AdminDashboardService(UserRepository userRepository, StudentRepository studentRepository,
                                TeacherRepository teacherRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", userRepository.count());
        summary.put("totalStudents", studentRepository.count());
        summary.put("totalTeachers", teacherRepository.count());
        summary.put("totalCourses", courseRepository.count());
        return summary;
    }
}
