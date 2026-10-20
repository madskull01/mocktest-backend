package com.example.mocktest.dto.mocktest;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateMockTestRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    @NotNull
    @Min(1)
    private Integer totalMarks;

    @NotNull
    @Min(0)
    private Integer passingMarks;

    private Boolean freeAccess;

    private boolean active;
}
