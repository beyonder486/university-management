package com.university.management.dto;

import com.university.management.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public class EnrollmentDtos {

    public record CreateEnrollmentRequest(
            @NotNull(message = "Student id is required")
            Long studentId,

            @NotNull(message = "Course id is required")
            Long courseId
    ) {}

    public record EnrollmentResponse(
            Long id,
            Long studentId,
            String studentName,
            Long courseId,
            String courseCode,
            String courseTitle,
            EnrollmentStatus status,
            String enrolledAt
    ) {}
}
