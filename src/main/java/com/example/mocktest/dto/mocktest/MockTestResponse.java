package com.example.mocktest.dto.mocktest;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MockTestResponse {

    private Long id;

    private String title;

    private String description;

    private Integer durationMinutes;

    private Integer totalMarks;

    private Integer passingMarks;

    private boolean freeAccess;

    private boolean active;

    private Integer questionCount;

    private LocalDateTime createdAt;
}
