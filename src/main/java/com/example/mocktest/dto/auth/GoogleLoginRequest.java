package com.example.mocktest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank
    @Size(max = 8192)
    private String credential;
}
