package com.example.mocktest.dto.attempt;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StartTestResponse {

    private Long attemptId;

    private Long testId;

    private String testTitle;

    private LocalDateTime startedAt;

    private Integer durationMinutes;
}
