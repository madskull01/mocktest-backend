package com.example.mocktest.dto.mocktest;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddQuestionToTestRequest {

    @NotNull
    private Long questionId;

    @NotNull
    @Min(1)
    private Integer questionOrder;
}
