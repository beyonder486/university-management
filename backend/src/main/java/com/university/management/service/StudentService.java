package com.university.management.service;

import com.university.management.dto.StudentDtos.CreateStudentRequest;
import com.university.management.dto.StudentDtos.StudentResponse;
import com.university.management.dto.StudentDtos.UpdateStudentRequest;
import com.university.management.entity.Role;
import com.university.management.entity.Student;
import com.university.management.entity.UserEntity;
import com.university.management.exception.ConflictException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentResponse> listStudents() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public StudentResponse getStudent(Long id) {
        return toResponse(studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id)));
    }

    public StudentResponse getStudentByUserId(Long userId) {
        return toResponse(studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found")));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw new ConflictException("Student number already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        UserEntity user = new UserEntity(request.username(), request.email(), passwordEncoder.encode(request.password()), Role.STUDENT);
        user.setEnabled(true);
        userRepository.save(user);

        Student student = new Student(user, request.studentNumber(), request.firstName(), request.lastName(),
                request.department(), request.major(), request.year());
        user.setStudent(student);
        Student saved = studentRepository.save(student);
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request, UserEntity currentUser) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(student.getUser().getId())) {
            throw new AccessDeniedException("You can only update your own student record");
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            student.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            student.setLastName(request.lastName());
        }
        if (request.department() != null && !request.department().isBlank()) {
            student.setDepartment(request.department());
        }
        if (request.major() != null) {
            student.setMajor(request.major());
        }
        if (request.year() != null) {
            student.setYear(request.year());
        }
        if (request.enabled() != null && currentUser.getRole() == Role.ADMIN) {
            student.getUser().setEnabled(request.enabled());
        }

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(Long id, UserEntity currentUser) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(student.getUser().getId())) {
            throw new AccessDeniedException("You cannot delete this student record");
        }

        userRepository.delete(student.getUser());
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getDepartment(),
                student.getMajor(),
                student.getYear(),
                student.getUser().getUsernameValue(),
                student.getUser().getEmail(),
                student.getUser().isEnabled()
        );
    }
}
