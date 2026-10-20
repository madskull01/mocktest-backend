package com.example.mocktest.security;

import com.example.mocktest.model.enums.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "unit-test-jwt-secret-that-is-long-enough-for-hmac";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signedTokenIncludesIdentityAndRoleAndAuthenticatesRequest() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 3_600_000);
        String token = jwtService.generateToken(42L, "admin@example.com", Role.ADMIN);
        Claims claims = jwtService.parseToken(token);

        assertEquals("admin@example.com", claims.getSubject());
        assertEquals(42, ((Number) claims.get("uid")).longValue());
        assertEquals("ADMIN", claims.get("role", String.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        new JwtAuthenticationFilter(jwtService).doFilter(request, response, chain);

        AuthenticatedUser principal = assertInstanceOf(
                AuthenticatedUser.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        assertEquals(42L, principal.userId());
        assertEquals(Role.ADMIN, principal.role());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void invalidTokenDoesNotAuthenticateRequest() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 3_600_000);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtAuthenticationFilter(jwtService).doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}