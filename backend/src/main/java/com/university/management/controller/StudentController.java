package com.university.management.controller;

import com.university.management.dto.StudentDtos.CreateStudentRequest;
import com.university.management.dto.StudentDtos.StudentResponse;
import com.university.management.dto.StudentDtos.UpdateStudentRequest;
import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> listStudents() {
        return ResponseEntity.ok(studentService.listStudents());
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyStudentProfile(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(studentService.getStudentByUserId(currentUser.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(studentService.createStudent(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        StudentResponse student = studentService.getStudent(id);
        if (currentUser.getRole() == Role.STUDENT && !student.username().equals(currentUser.getUsernameValue())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only view your own student record");
        }
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateStudentRequest request,
                                                     @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(studentService.updateStudent(id, request, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        studentService.deleteStudent(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
