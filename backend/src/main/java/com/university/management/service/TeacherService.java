package com.university.management.service;

import com.university.management.dto.TeacherDtos.CreateTeacherRequest;
import com.university.management.dto.TeacherDtos.TeacherResponse;
import com.university.management.dto.TeacherDtos.UpdateTeacherRequest;
import com.university.management.entity.Role;
import com.university.management.entity.Teacher;
import com.university.management.entity.UserEntity;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.TeacherRepository;
import com.university.management.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TeacherResponse> listTeachers() {
        return teacherRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TeacherResponse getTeacher(Long id) {
        return toResponse(teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id)));
    }

    public TeacherResponse getTeacherByUserId(Long userId) {
        return toResponse(teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found")));
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        UserEntity user = new UserEntity(request.username(), request.email(), passwordEncoder.encode(request.password()), Role.TEACHER);
        user.setEnabled(true);
        userRepository.save(user);

        Teacher teacher = new Teacher(user, request.firstName(), request.lastName(), request.department(), request.title());
        user.setTeacher(teacher);
        Teacher saved = teacherRepository.save(teacher);
        return toResponse(saved);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request, UserEntity currentUser) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(teacher.getUser().getId())) {
            throw new AccessDeniedException("You can only update your own teacher profile");
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            teacher.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            teacher.setLastName(request.lastName());
        }
        if (request.department() != null && !request.department().isBlank()) {
            teacher.setDepartment(request.department());
        }
        if (request.title() != null) {
            teacher.setTitle(request.title());
        }
        if (request.enabled() != null && currentUser.getRole() == Role.ADMIN) {
            teacher.getUser().setEnabled(request.enabled());
        }

        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteTeacher(Long id, UserEntity currentUser) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(teacher.getUser().getId())) {
            throw new AccessDeniedException("You cannot delete this teacher record");
        }

        userRepository.delete(teacher.getUser());
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getDepartment(),
                teacher.getTitle(),
                teacher.getUser().getUsernameValue(),
                teacher.getUser().getEmail(),
                teacher.getUser().isEnabled()
        );
    }
}
