package com.university.management.controller;

import com.university.management.dto.TeacherDtos.CreateTeacherRequest;
import com.university.management.dto.TeacherDtos.TeacherResponse;
import com.university.management.dto.TeacherDtos.UpdateTeacherRequest;
import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public ResponseEntity<List<TeacherResponse>> listTeachers() {
        return ResponseEntity.ok(teacherService.listTeachers());
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/me")
    public ResponseEntity<TeacherResponse> getMyTeacherProfile(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(teacherService.getTeacherByUserId(currentUser.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.ok(teacherService.createTeacher(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        TeacherResponse teacher = teacherService.getTeacher(id);
        if (currentUser.getRole() == Role.TEACHER && !teacher.username().equals(currentUser.getUsernameValue())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only view your own teacher profile");
        }
        return ResponseEntity.ok(teacher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateTeacherRequest request,
                                                       @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        teacherService.deleteTeacher(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
