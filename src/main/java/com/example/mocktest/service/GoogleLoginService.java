package com.example.mocktest.service;

import com.example.mocktest.dto.auth.LoginResponse;
import com.example.mocktest.exception.BadRequestException;
import com.example.mocktest.exception.UnauthorizedException;
import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.Role;
import com.example.mocktest.repository.UserRepository;
import com.example.mocktest.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleLoginService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JwtDecoder googleJwtDecoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.oauth.google.client-id:}")
    private String clientId;

    public LoginResponse login(String credential) {
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("Google sign-in is not configured");
        }

        Jwt googleToken;
        try {
            googleToken = googleJwtDecoder.decode(credential);
        } catch (JwtException exception) {
            throw new UnauthorizedException("Google credential is invalid or expired");
        }

        String email = googleToken.getClaimAsString("email");
        String subject = googleToken.getSubject();
        if (!Boolean.TRUE.equals(googleToken.getClaim("email_verified"))
                || email == null || email.isBlank()
                || !email.contains("@")
                || subject == null || subject.isBlank()) {
            throw new UnauthorizedException(
                    "Google account must have a verified email address");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> createGoogleUser(
                        normalizedEmail,
                        googleToken.getClaimAsString("name")));
        if (!user.isEnabled()) {
            throw new UnauthorizedException("This account is disabled");
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtService.generateToken(
                        user.getId(), user.getEmail(), user.getRole()))
                .build();
    }

    private User createGoogleUser(String email, String name) {
        LocalDateTime now = LocalDateTime.now();
        String displayName = name == null || name.isBlank()
                ? email.substring(0, email.indexOf('@'))
                : name.trim();
        byte[] randomBytes = new byte[48];
        SECURE_RANDOM.nextBytes(randomBytes);
        User user = User.builder()
                .name(displayName)
                .email(email)
                .password(passwordEncoder.encode(
                        Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)))
                .role(Role.STUDENT)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }
}
