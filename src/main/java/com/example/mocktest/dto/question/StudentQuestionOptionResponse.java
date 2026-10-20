package com.example.mocktest.dto.question;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentQuestionOptionResponse {

    private Long id;

    private String optionText;
}
