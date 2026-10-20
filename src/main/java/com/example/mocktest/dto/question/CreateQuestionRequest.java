package com.example.mocktest.dto.question;

import com.example.mocktest.model.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionRequest {

    @NotBlank
    private String questionText;

    private String explanation;

    @NotNull
    @Min(1)
    private Integer marks;

    @Min(0)
    private Integer negativeMarks;

    @NotNull
    private Difficulty difficulty;

    @Valid
    @NotNull
    @Size(min = 4, max = 4)
    private List<CreateQuestionOptionRequest> options;
}
