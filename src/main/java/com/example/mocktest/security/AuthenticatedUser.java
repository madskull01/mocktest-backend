package com.example.mocktest.security;

import com.example.mocktest.model.enums.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}