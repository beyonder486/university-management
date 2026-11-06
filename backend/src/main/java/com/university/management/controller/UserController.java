package com.university.management.controller;

import com.university.management.dto.UserDtos.CreateUserRequest;
import com.university.management.dto.UserDtos.UpdateUserProfileRequest;
import com.university.management.dto.UserDtos.UserResponse;
import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(id)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only view your own account");
        }
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                 @RequestBody UpdateUserProfileRequest request,
                                                 @AuthenticationPrincipal UserEntity currentUser) {
        return ResponseEntity.ok(userService.updateProfile(id, request, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserEntity currentUser) {
        userService.deleteUser(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
