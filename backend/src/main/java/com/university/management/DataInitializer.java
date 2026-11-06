package com.university.management;

import com.university.management.entity.Role;
import com.university.management.entity.UserEntity;
import com.university.management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            UserEntity admin = new UserEntity("admin", "admin@university.edu", passwordEncoder.encode("Admin123!"), Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }
}
