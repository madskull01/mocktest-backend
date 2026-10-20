package com.example.mocktest.dto.question;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQuestionOptionRequest {

    @NotBlank
    private String optionText;

    private boolean correct;
}
