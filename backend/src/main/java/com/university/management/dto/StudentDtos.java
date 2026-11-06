package com.university.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StudentDtos {

    public record CreateStudentRequest(
            @NotBlank(message = "Student number is required")
            String studentNumber,

            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotBlank(message = "Department is required")
            String department,

            String major,

            @NotNull(message = "Year is required")
            @Positive(message = "Year must be positive")
            Integer year,

            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record UpdateStudentRequest(
            String firstName,
            String lastName,
            String department,
            String major,
            Integer year,
            Boolean enabled
    ) {}

    public record StudentResponse(
            Long id,
            String studentNumber,
            String firstName,
            String lastName,
            String department,
            String major,
            Integer year,
            String username,
            String email,
            Boolean enabled
    ) {}
}
