package com.example.mocktest.dto.subject;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSubjectRequest {

    @NotBlank
    private String name;

    private String description;

    private boolean active;
}
