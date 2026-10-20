package com.example.mocktest.service;

import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.Role;
import com.example.mocktest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountInitializerTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "a-strong-password";

    @Test
    void createsAdminWithEncodedPasswordWhenConfigured() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(ADMIN_PASSWORD)).thenReturn("encoded-password");
        AdminAccountInitializer initializer = initializer(
                userRepository, passwordEncoder, "  ADMIN@example.com  ", ADMIN_PASSWORD);

        initializer.run(mock(ApplicationArguments.class));

        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("Administrator", savedUser.getValue().getName());
        assertEquals(ADMIN_EMAIL, savedUser.getValue().getEmail());
        assertEquals("encoded-password", savedUser.getValue().getPassword());
        assertEquals(Role.ADMIN, savedUser.getValue().getRole());
        assertNotNull(savedUser.getValue().getCreatedAt());
        verify(passwordEncoder).encode(ADMIN_PASSWORD);
    }

    @Test
    void skipsProvisioningWhenEnvironmentIsNotConfigured() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AdminAccountInitializer initializer =
                initializer(userRepository, passwordEncoder, "", "");

        initializer.run(mock(ApplicationArguments.class));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsPartialAdminConfiguration() {
        AdminAccountInitializer initializer = initializer(
                mock(UserRepository.class),
                mock(PasswordEncoder.class),
                ADMIN_EMAIL,
                "");

        assertThrows(
                IllegalStateException.class,
                () -> initializer.run(mock(ApplicationArguments.class)));
    }

    @Test
    void preservesExistingAdminCredentials() {
        User existingAdmin = User.builder()
                .email(ADMIN_EMAIL)
                .role(Role.ADMIN)
                .build();
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.findByEmail(ADMIN_EMAIL))
                .thenReturn(Optional.of(existingAdmin));
        AdminAccountInitializer initializer =
                initializer(userRepository, passwordEncoder, ADMIN_EMAIL, ADMIN_PASSWORD);

        initializer.run(mock(ApplicationArguments.class));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void refusesToPromoteAnExistingStudentAccount() {
        User existingStudent = User.builder()
                .email(ADMIN_EMAIL)
                .role(Role.STUDENT)
                .build();
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail(ADMIN_EMAIL))
                .thenReturn(Optional.of(existingStudent));
        AdminAccountInitializer initializer = initializer(
                userRepository,
                mock(PasswordEncoder.class),
                ADMIN_EMAIL,
                ADMIN_PASSWORD);

        assertThrows(
                IllegalStateException.class,
                () -> initializer.run(mock(ApplicationArguments.class)));

        assertEquals(Role.STUDENT, existingStudent.getRole());
        verify(userRepository, never()).save(any(User.class));
    }

    private AdminAccountInitializer initializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String password) {
        return new AdminAccountInitializer(
                userRepository,
                passwordEncoder,
                "Administrator",
                email,
                password);
    }
}
