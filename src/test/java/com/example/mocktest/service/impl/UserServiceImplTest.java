package com.example.mocktest.service.impl;

import com.example.mocktest.dto.auth.LoginRequest;
import com.example.mocktest.dto.auth.LoginResponse;
import com.example.mocktest.dto.user.CreateUserRequest;
import com.example.mocktest.exception.UnauthorizedException;
import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.Role;
import com.example.mocktest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void authenticateReturnsUserDetailsForBcryptPassword() {
        User user = createUser(passwordEncoder.encode("correct-password"), true);
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));

        LoginResponse response = userService.authenticate(
                loginRequest("correct-password")
        );

        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(null, response.getToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserStoresBcryptPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Student");
        request.setEmail("student@example.com");
        request.setPassword("new-password");
        request.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        var savedUser = forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertTrue(passwordEncoder.matches("new-password", savedUser.getValue().getPassword()));
        assertEquals(Role.STUDENT, savedUser.getValue().getRole());
    }

    @Test
    void authenticateUpgradesLegacyPlaintextPassword() {
        User user = createUser("legacy-password", true);
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.authenticate(loginRequest("legacy-password"));

        assertTrue(passwordEncoder.matches("legacy-password", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void authenticateRejectsIncorrectPassword() {
        User user = createUser(passwordEncoder.encode("correct-password"), true);
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UnauthorizedException.class,
                () -> userService.authenticate(loginRequest("wrong-password"))
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateRejectsDisabledUser() {
        User user = createUser(passwordEncoder.encode("correct-password"), false);
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UnauthorizedException.class,
                () -> userService.authenticate(loginRequest("correct-password"))
        );
    }

    private User createUser(String password, boolean enabled) {
        return User.builder()
                .id(7L)
                .name("Student")
                .email("student@example.com")
                .password(password)
                .role(Role.STUDENT)
                .enabled(enabled)
                .build();
    }

    private LoginRequest loginRequest(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail("student@example.com");
        request.setPassword(password);
        return request;
    }
}