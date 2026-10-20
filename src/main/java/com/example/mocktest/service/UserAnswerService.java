package com.example.mocktest.service;


import com.example.mocktest.dto.attempt.StudentAnswerResponse;
import com.example.mocktest.dto.attempt.SubmitAnswerRequest;
import com.example.mocktest.model.UserAnswer;

import java.util.List;

public interface UserAnswerService {

    StudentAnswerResponse saveAnswer(
            Long attemptId,
            Long userId,
            SubmitAnswerRequest request
    );

    List<StudentAnswerResponse> getAnswersByAttempt(
            Long attemptId,
            Long userId
    );
}