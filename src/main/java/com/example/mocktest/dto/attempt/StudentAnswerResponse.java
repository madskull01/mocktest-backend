package com.example.mocktest.dto.attempt;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAnswerResponse {

    private Long questionId;

    private Long selectedOptionId;
}
