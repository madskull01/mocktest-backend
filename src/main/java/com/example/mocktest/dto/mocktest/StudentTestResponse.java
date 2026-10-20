package com.example.mocktest.dto.mocktest;
import com.example.mocktest.dto.question.StudentQuestionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentTestResponse {

    private Long testId;

    private String title;

    private String description;

    private Integer durationMinutes;

    private Integer totalMarks;

    private Integer totalQuestions;

    private List<StudentQuestionResponse> questions;
}
