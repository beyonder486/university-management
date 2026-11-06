package com.university.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CourseDtos {

    public record CreateCourseRequest(
            @NotBlank(message = "Course code is required")
            String courseCode,

            @NotBlank(message = "Title is required")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            @NotNull(message = "Credits are required")
            @Positive(message = "Credits must be positive")
            Integer credits,

            Long teacherId,
            Integer maxStudents
    ) {}

    public record UpdateCourseRequest(
            String courseCode,
            String title,
            String description,
            Integer credits,
            Long teacherId,
            Integer maxStudents
    ) {}

    public record CourseResponse(
            Long id,
            String courseCode,
            String title,
            String description,
            Integer credits,
            Long teacherId,
            String teacherName,
            Integer maxStudents
    ) {}
}
