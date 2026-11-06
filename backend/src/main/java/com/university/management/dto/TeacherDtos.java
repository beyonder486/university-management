package com.university.management.dto;

import jakarta.validation.constraints.NotBlank;

public class TeacherDtos {

    public record CreateTeacherRequest(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotBlank(message = "Department is required")
            String department,

            String title,

            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record UpdateTeacherRequest(
            String firstName,
            String lastName,
            String department,
            String title,
            Boolean enabled
    ) {}

    public record TeacherResponse(
            Long id,
            String firstName,
            String lastName,
            String department,
            String title,
            String username,
            String email,
            Boolean enabled
    ) {}
}
