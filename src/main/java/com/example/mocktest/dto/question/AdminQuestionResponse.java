package com.example.mocktest.dto.question;

import com.example.mocktest.model.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminQuestionResponse {

    private Long id;

    private String questionText;

    private String imageUrl;

    private String explanation;

    private Integer marks;

    private Integer negativeMarks;

    private Difficulty difficulty;

    private boolean active;

    private Long subjectId;

    private String subjectName;

    private List<QuestionOptionResponse> options;
}
