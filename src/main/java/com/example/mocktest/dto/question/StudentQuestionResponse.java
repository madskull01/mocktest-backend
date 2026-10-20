package com.example.mocktest.dto.question;

import com.example.mocktest.model.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentQuestionResponse {

    private Long id;

    private String questionText;

    private String imageUrl;

    private Integer marks;

    private Integer negativeMarks;

    private Difficulty difficulty;

    private List<StudentQuestionOptionResponse> options;
}
