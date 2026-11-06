package com.university.management.controller;

import com.university.management.dto.AuthDtos.AuthResponse;
import com.university.management.dto.AuthDtos.LoginRequest;
import com.university.management.dto.UserDtos.UserResponse;
import com.university.management.entity.UserEntity;
import com.university.management.service.AuthService;
import com.university.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(userService.getCurrentUser(currentUser));
    }
}
