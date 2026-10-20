package com.example.mocktest.dto.attempt;


import com.example.mocktest.model.enums.AttemptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttemptHistoryResponse {

    private Long attemptId;

    private Long testId;

    private String testTitle;

    private Integer score;

    private Integer totalMarks;

    private Double percentage;

    private AttemptStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;
}
