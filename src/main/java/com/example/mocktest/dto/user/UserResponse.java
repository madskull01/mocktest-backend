package com.example.mocktest.dto.user;

import com.example.mocktest.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private boolean enabled;
}