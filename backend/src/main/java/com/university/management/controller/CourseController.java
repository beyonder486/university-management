package com.university.management.controller;

import com.university.management.dto.CourseDtos.CourseResponse;
import com.university.management.dto.CourseDtos.CreateCourseRequest;
import com.university.management.dto.CourseDtos.UpdateCourseRequest;
import com.university.management.entity.UserEntity;
import com.university.management.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(courseService.listCourses(currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request,
                                                     @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(courseService.createCourse(request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(courseService.getCourse(id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                     @RequestBody UpdateCourseRequest request,
                                                     @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(courseService.updateCourse(id, request, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        courseService.deleteCourse(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
