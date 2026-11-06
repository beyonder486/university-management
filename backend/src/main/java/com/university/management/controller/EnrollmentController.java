package com.university.management.controller;

import com.university.management.dto.EnrollmentDtos.CreateEnrollmentRequest;
import com.university.management.dto.EnrollmentDtos.EnrollmentResponse;
import com.university.management.entity.UserEntity;
import com.university.management.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> listEnrollments(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(enrollmentService.listEnrollments(currentUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @PostMapping
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request,
                                                             @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(enrollmentService.createEnrollment(request, currentUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdrawEnrollment(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        enrollmentService.withdrawEnrollment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
