package com.university.management.repository;

import com.university.management.entity.Course;
import com.university.management.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCourseCode(String courseCode);
    List<Course> findByTeacher(Teacher teacher);
}
