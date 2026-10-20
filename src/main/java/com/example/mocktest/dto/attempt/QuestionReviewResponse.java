package com.example.mocktest.dto.attempt;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionReviewResponse {

    private Long questionId;

    private Integer questionNumber;

    private String questionText;

    private String imageUrl;

    private List<AnswerOptionReviewResponse> options;

    private Long selectedOptionId;

    private Long correctOptionId;

    private String explanation;

    private boolean answered;

    private boolean correct;

    private Integer marksAwarded;

    private Integer marksDeducted;
}
