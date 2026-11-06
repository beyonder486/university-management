package com.university.management.service;

import com.university.management.dto.UserDtos.CreateUserRequest;
import com.university.management.dto.UserDtos.UpdateUserProfileRequest;
import com.university.management.dto.UserDtos.UserResponse;
import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getCurrentUser(UserEntity currentUser) {
        return toResponse(userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public UserResponse getUser(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        Role role = parseRole(request.role());
        UserEntity user = new UserEntity(request.username(), request.email(), passwordEncoder.encode(request.password()), role);
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserProfileRequest request, UserEntity currentUser) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        if (request.username() != null && !request.username().isBlank()) {
            if (!request.username().equals(user.getUsernameValue()) && userRepository.existsByUsername(request.username())) {
                throw new ConflictException("Username already exists");
            }
            user.setUsername(request.username());
        }
        if (request.email() != null && !request.email().isBlank()) {
            if (!request.email().equals(user.getEmail()) && userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email already registered");
            }
            user.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId, UserEntity currentUser) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot delete your own account");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can delete users");
        }

        if (user.getRole() == Role.ADMIN && userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count() <= 1) {
            throw new AccessDeniedException("At least one administrator must remain in the system");
        }

        userRepository.delete(user);
    }

    public static Role parseRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        try {
            return Role.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + requestedRole);
        }
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsernameValue(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled()
        );
    }
}
