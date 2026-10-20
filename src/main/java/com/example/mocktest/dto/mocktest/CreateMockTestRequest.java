package com.example.mocktest.dto.mocktest;



import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMockTestRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
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

    private boolean freeAccess;
}