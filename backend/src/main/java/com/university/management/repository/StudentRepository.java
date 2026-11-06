package com.university.management.repository;

import com.university.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String studentNumber);
    Optional<Student> findByUserId(Long userId);
    boolean existsByStudentNumber(String studentNumber);
}
