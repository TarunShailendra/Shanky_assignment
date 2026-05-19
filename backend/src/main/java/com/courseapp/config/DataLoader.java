package com.courseapp.config;

import com.courseapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        fixPasswordHash("alice.johnson@university.edu", "password123");
        fixPasswordHash("bob.smith@university.edu",      "password123");
        fixPasswordHash("carol.davis@university.edu",     "password123");
        fixPasswordHash("student1@email.com",  "password123");
        fixPasswordHash("student2@email.com",  "password123");
        fixPasswordHash("student3@email.com",  "password123");
        fixPasswordHash("student4@email.com",  "password123");
        fixPasswordHash("student5@email.com",  "password123");
    }

    private void fixPasswordHash(String email, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                String newHash = passwordEncoder.encode(rawPassword);
                int updated = userRepository.updatePasswordHash(email, newHash);
                log.info("Password hash corrected for {} ({} row(s) updated)", email, updated);
            }
        });
    }
}