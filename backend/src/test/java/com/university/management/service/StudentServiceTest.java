package com.university.management.service;

import com.university.management.dto.StudentDtos.CreateStudentRequest;
import com.university.management.dto.StudentDtos.StudentResponse;
import com.university.management.dto.StudentDtos.UpdateStudentRequest;
import com.university.management.entity.Role;
import com.university.management.entity.Student;
import com.university.management.entity.UserEntity;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void listStudentsMapsEntitiesToResponses() {
        UserEntity user = new UserEntity("sally", "sally@example.edu", "encoded", Role.STUDENT);
        Student student = new Student(user, "S-100", "Sally", "Student", "Science", "Biology", 2);
        student.setId(12L);
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<StudentResponse> result = studentService.listStudents();

        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).id());
        assertEquals("S-100", result.get(0).studentNumber());
        assertEquals("sally@example.edu", result.get(0).email());
    }

    @Test
    void createStudentEncodesPasswordAndPersistsUserAndProfile() {
        CreateStudentRequest request = new CreateStudentRequest(
                "S-101", "Sam", "Student", "Engineering", "Civil", 1,
                "sam@example.edu", "samstudent", "password-123"
        );
        when(studentRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("bcrypt-hash");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(21L);
            return saved;
        });

        StudentResponse result = studentService.createStudent(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(userRepository).save(userCaptor.capture());
        verify(studentRepository).save(studentCaptor.capture());
        assertEquals("bcrypt-hash", userCaptor.getValue().getPassword());
        assertEquals(Role.STUDENT, userCaptor.getValue().getRole());
        assertEquals(userCaptor.getValue(), studentCaptor.getValue().getUser());
        assertEquals(21L, result.id());
        assertEquals("S-101", result.studentNumber());
    }

    @Test
    void updateStudentRejectsChangesToAnotherUsersProfile() {
        UserEntity owner = new UserEntity("owner", "owner@example.edu", "encoded", Role.STUDENT);
        owner.setId(1L);
        UserEntity otherStudent = new UserEntity("other", "other@example.edu", "encoded", Role.STUDENT);
        otherStudent.setId(2L);
        Student student = new Student(owner, "S-100", "Sally", "Student", "Science", "Biology", 2);
        when(studentRepository.findById(12L)).thenReturn(Optional.of(student));

        assertThrows(AccessDeniedException.class, () -> studentService.updateStudent(
                12L, new UpdateStudentRequest("Changed", null, null, null, null, null), otherStudent
        ));

        verify(studentRepository, never()).save(any(Student.class));
    }
}