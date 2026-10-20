package com.example.mocktest.service;

import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.Role;
import com.example.mocktest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
@Slf4j
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:Administrator}") String adminName,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean hasEmail = adminEmail != null && !adminEmail.isBlank();
        boolean hasPassword = adminPassword != null && !adminPassword.isBlank();
        if (!hasEmail && !hasPassword) {
            log.info("Admin bootstrap is not configured; no admin account was created");
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "Set both ADMIN_EMAIL and ADMIN_PASSWORD to provision the admin account");
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        String normalizedName = adminName == null || adminName.isBlank()
                ? "Administrator"
                : adminName.trim();
        if (!normalizedEmail.contains("@") || normalizedEmail.length() > 254) {
            throw new IllegalStateException("ADMIN_EMAIL must be a valid email address");
        }
        if (normalizedName.length() > 100) {
            throw new IllegalStateException("ADMIN_NAME must be 100 characters or fewer");
        }
        if (adminPassword.length() < 6 || adminPassword.length() > 100) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD must be between 6 and 100 characters");
        }

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(user -> {
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                        "ADMIN_EMAIL is already assigned to a non-admin account");
            }
            log.info("Configured admin account already exists; existing credentials were preserved");
        }, () -> {
            LocalDateTime now = LocalDateTime.now();
            User admin = User.builder()
                    .name(normalizedName)
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(admin);
            log.info("Admin account was provisioned from environment configuration");
        });
    }
}
