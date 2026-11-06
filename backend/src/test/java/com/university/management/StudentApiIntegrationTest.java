package com.university.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.management.entity.Role;
import com.university.management.entity.Student;
import com.university.management.entity.UserEntity;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentApiIntegrationTest {

    private static final String PASSWORD = "test-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetDatabase() {
        studentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void adminCanLogInAndReadStudentJson() throws Exception {
        createUser("admin", "admin@example.edu", Role.ADMIN);
        Student student = createStudent("sally", "sally@example.edu", "S-201");
        String token = login("admin@example.edu");

        mockMvc.perform(get("/api/students/{id}", student.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber", is("S-201")))
                .andExpect(jsonPath("$.firstName", is("Sally")))
                .andExpect(jsonPath("$.email", is("sally@example.edu")));
    }

    @Test
    void studentMustAuthenticateAndCanOnlyReadOwnRecord() throws Exception {
        Student ownStudent = createStudent("sally", "sally@example.edu", "S-202");
        Student anotherStudent = createStudent("alex", "alex@example.edu", "S-203");

        mockMvc.perform(get("/api/students/{id}", ownStudent.getId()))
                .andExpect(status().isUnauthorized());

        String token = login("sally@example.edu");
        mockMvc.perform(get("/api/students/{id}", ownStudent.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber", is("S-202")));

        mockMvc.perform(get("/api/students/{id}", anotherStudent.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateStudentAndReadPersistedRecord() throws Exception {
        createUser("admin", "admin@example.edu", Role.ADMIN);
        String token = login("admin@example.edu");
        String requestBody = """
                {
                  "studentNumber": "S-204",
                  "firstName": "Taylor",
                  "lastName": "Student",
                  "department": "Engineering",
                  "major": "Software",
                  "year": 1,
                  "email": "taylor@example.edu",
                  "username": "taylorstudent",
                  "password": "student-password-123"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentNumber", is("S-204")))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long studentId = created.get("id").asLong();
        mockMvc.perform(get("/api/students/{id}", studentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Taylor")))
                .andExpect(jsonPath("$.email", is("taylor@example.edu")));
    }

    private Student createStudent(String username, String email, String studentNumber) {
        UserEntity user = createUser(username, email, Role.STUDENT);
        Student student = new Student(user, studentNumber, "Sally", "Student", "Science", "Biology", 2);
        user.setStudent(student);
        return studentRepository.saveAndFlush(student);
    }

    private UserEntity createUser(String username, String email, Role role) {
        UserEntity user = new UserEntity(username, email, passwordEncoder.encode(PASSWORD), role);
        user.setEnabled(true);
        return userRepository.saveAndFlush(user);
    }

    private String login(String email) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "email", email,
                "password", PASSWORD
        ));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}