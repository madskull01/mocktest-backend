package com.example.mocktest.dto.attempt;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAnswerResponse {

    private Long questionId;

    private Long selectedOptionId;

    private Boolean correct;

    private Integer marksObtained;
}
