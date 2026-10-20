package com.example.mocktest.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsAllowsConfiguredNetlifyOriginAndRejectsOtherOrigins() {
        String netlifyOrigin = "https://profound-dolphin-979043.netlify.app";
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class));
        ReflectionTestUtils.setField(
                securityConfig,
                "allowedOrigins",
                "http://localhost:5173, " + netlifyOrigin);

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/attempts/history"));

        assertNotNull(cors);
        assertEquals(netlifyOrigin, cors.checkOrigin(netlifyOrigin));
        assertNull(cors.checkOrigin("https://untrusted.example"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(cors.getAllowCredentials());
    }
}
