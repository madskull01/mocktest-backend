package com.example.mocktest.dto.auth;

import com.example.mocktest.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;

    private Long userId;

    private String name;

    private String email;

    private Role role;
}