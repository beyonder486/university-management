package com.university.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {

    public record CreateUserRequest(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @NotBlank(message = "Role is required")
            String role
    ) {}

    public record UpdateUserProfileRequest(
            String username,
            String email,
            String password
    ) {}

    public record UserResponse(
            Long id,
            String username,
            String email,
            String role,
            Boolean enabled
    ) {}
}
