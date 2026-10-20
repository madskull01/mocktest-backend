package com.example.mocktest.service;

import com.example.mocktest.exception.BadRequestException;
import com.example.mocktest.exception.UnauthorizedException;
import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.Role;
import com.example.mocktest.repository.UserRepository;
import com.example.mocktest.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleLoginServiceTest {

    private static final String CLIENT_ID = "google-web-client-id";
    private JwtDecoder jwtDecoder;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private GoogleLoginService googleLoginService;

    @BeforeEach
    void setUp() {
        jwtDecoder = mock(JwtDecoder.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = mock(JwtService.class);
        googleLoginService = new GoogleLoginService(
                jwtDecoder, userRepository, passwordEncoder, jwtService);
        ReflectionTestUtils.setField(googleLoginService, "clientId", CLIENT_ID);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("app-jwt");
    }

    @Test
    void loginCreatesStudentAccountAndReturnsApplicationJwt() {
        when(jwtDecoder.decode("google-credential"))
                .thenReturn(googleToken(true));
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(42L);
                    return user;
                });

        var response = googleLoginService.login("google-credential");

        assertEquals(42L, response.getUserId());
        assertEquals("student@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals("app-jwt", response.getToken());
        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("Google Student", savedUser.getValue().getName());
        assertTrue(savedUser.getValue().getPassword().startsWith("$2"));
    }

    @Test
    void loginRejectsUnverifiedGoogleEmail() {
        when(jwtDecoder.decode("google-credential"))
                .thenReturn(googleToken(false));

        assertThrows(
                UnauthorizedException.class,
                () -> googleLoginService.login("google-credential")
        );

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginUsesExistingAccountAndPreservesItsRole() {
        User admin = User.builder()
                .id(7L)
                .name("Admin")
                .email("student@example.com")
                .password("existing-password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(jwtDecoder.decode("google-credential"))
                .thenReturn(googleToken(true));
        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(admin));

        var response = googleLoginService.login("google-credential");

        assertEquals(Role.ADMIN, response.getRole());
        assertEquals(admin.getId(), response.getUserId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginRejectsWhenGoogleClientIsNotConfigured() {
        ReflectionTestUtils.setField(googleLoginService, "clientId", "");

        assertThrows(
                BadRequestException.class,
                () -> googleLoginService.login("google-credential")
        );

        verify(jwtDecoder, never()).decode(any());
    }

    private Jwt googleToken(boolean emailVerified) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("verified-google-token")
                .header("alg", "RS256")
                .issuer("https://accounts.google.com")
                .subject("google-user-123")
                .audience(List.of(CLIENT_ID))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("email", "Student@Example.com")
                .claim("email_verified", emailVerified)
                .claim("name", "Google Student")
                .build();
    }
}
