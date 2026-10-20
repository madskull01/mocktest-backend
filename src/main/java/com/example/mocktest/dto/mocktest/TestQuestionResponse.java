package com.example.mocktest.dto.mocktest;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestQuestionResponse {

    private Long id;

    private Long questionId;

    private Long subjectId;

    private String subjectName;

    private Integer questionOrder;

    private Integer marks;
}
