package com.example.mocktest.dto.attempt;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitAnswerRequest {

    @NotNull
    private Long questionId;

    @NotNull
    private Long optionId;
}
