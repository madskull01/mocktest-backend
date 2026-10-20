package com.example.mocktest.dto.section;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSectionRequest {

    @NotBlank
    private String name;

    private String description;

    private boolean active;
}
