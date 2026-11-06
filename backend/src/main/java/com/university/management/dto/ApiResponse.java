package com.university.management.dto;

import java.time.LocalDateTime;

public record ApiResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path
) {
}
