package com.university.management.service;

import com.university.management.dto.AuthDtos.LoginRequest;
import com.university.management.dto.AuthDtos.AuthResponse;
import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.repository.UserRepository;
import com.university.management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "c2VjcmV0LXN0cmluZy1mb3ItdW5pdmVyc2l0eS1tYW5hZ2VtZW50LXByb2plY3Q=");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("admin@example.edu", "correct-password");
        UserEntity user = new UserEntity("admin", request.email(), "encoded-password", Role.ADMIN);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        AuthResponse result = authService.login(request);

        assertEquals("admin@example.edu", jwtService.extractUsername(result.token()));
        assertEquals("Bearer", result.tokenType());
        assertEquals("ADMIN", result.role());
    }

    @Test
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest("missing@example.edu", "correct-password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        LoginRequest request = new LoginRequest("admin@example.edu", "wrong-password");
        UserEntity user = new UserEntity("admin", request.email(), "encoded-password", Role.ADMIN);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}