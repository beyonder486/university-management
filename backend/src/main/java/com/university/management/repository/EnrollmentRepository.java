package com.university.management.repository;

import com.university.management.entity.Course;
import com.university.management.entity.Enrollment;
import com.university.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByCourse(Course course);
    List<Enrollment> findByStudent_User_Id(Long userId);
    List<Enrollment> findByCourse_Teacher_User_Id(Long userId);
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    long countByCourse(Course course);
}
