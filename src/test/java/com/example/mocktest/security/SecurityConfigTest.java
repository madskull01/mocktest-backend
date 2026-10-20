package com.example.mocktest.security;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.DefaultCorsProcessor;

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
                "http://localhost:5173,https://cdacmocktest.com," + netlifyOrigin);

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/attempts/history"));

        assertNotNull(cors);
        assertEquals("https://cdacmocktest.com",
                cors.checkOrigin("https://cdacmocktest.com"));
        assertEquals(netlifyOrigin, cors.checkOrigin(netlifyOrigin));
        assertNull(cors.checkOrigin("https://untrusted.example"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(cors.getAllowCredentials());
    }

    @Test
    void corsPreflightReturnsExpectedHeadersForLoginAndCatalogRequests() throws IOException {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class));
        ReflectionTestUtils.setField(
                securityConfig,
                "allowedOrigins",
                "https://cdacmocktest.com");
        CorsConfiguration cors = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(
                        new MockHttpServletRequest("OPTIONS", "/api/auth/login"));
        assertNotNull(cors);

        assertPreflightAccepted(cors, "/api/auth/login", "POST", "content-type");
        assertPreflightAccepted(
                cors,
                "/api/mock-tests/catalog",
                "GET",
                "authorization,content-type");
    }

    private void assertPreflightAccepted(
            CorsConfiguration cors,
            String path,
            String requestedMethod,
            String requestedHeaders) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", path);
        request.addHeader("Origin", "https://cdacmocktest.com");
        request.addHeader("Access-Control-Request-Method", requestedMethod);
        request.addHeader("Access-Control-Request-Headers", requestedHeaders);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean accepted = new DefaultCorsProcessor()
                .processRequest(cors, request, response);

        assertTrue(accepted, path + " preflight should be accepted");
        assertEquals("https://cdacmocktest.com",
                response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true",
                response.getHeader("Access-Control-Allow-Credentials"));
        assertTrue(response.getHeader("Access-Control-Allow-Methods")
                .contains(requestedMethod));
        String allowedHeaders = response.getHeader("Access-Control-Allow-Headers")
                .toLowerCase();
        for (String header : requestedHeaders.split(",")) {
            assertTrue(allowedHeaders.contains(header),
                    "Expected CORS to allow the " + header + " header");
        }
    }
}
