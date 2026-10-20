package com.example.mocktest.dto.attempt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerOptionReviewResponse {

    private Long optionId;

    private String optionText;

    private boolean correct;

    private boolean selected;
}
