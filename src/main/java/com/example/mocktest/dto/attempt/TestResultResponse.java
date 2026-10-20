package com.example.mocktest.dto.attempt;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestResultResponse {

    private Long attemptId;

    private Long testId;

    private String testTitle;

    private Integer score;

    private Integer totalMarks;

    private Integer correctAnswers;

    private Integer incorrectAnswers;

    private Integer unanswered;

    private Integer totalQuestions;

    private Integer attemptedQuestions;

    private Long timeTakenSeconds;

    private Double percentage;

    private Boolean passed;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;
}
