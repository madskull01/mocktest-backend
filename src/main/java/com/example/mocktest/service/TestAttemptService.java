package com.example.mocktest.service;


import com.example.mocktest.dto.attempt.AttemptHistoryResponse;
import com.example.mocktest.dto.attempt.StartTestResponse;
import com.example.mocktest.dto.attempt.TestResultResponse;
import com.example.mocktest.dto.attempt.QuestionReviewResponse;
import com.example.mocktest.dto.mocktest.StudentTestResponse;
import com.example.mocktest.model.TestAttempt;

import java.util.List;

public interface TestAttemptService {

    StartTestResponse startTest(
            Long userId,
            Long testId
    );

    StudentTestResponse getTestForAttempt(
            Long attemptId,
            Long userId
    );

    TestResultResponse submitTest(
            Long attemptId,
            Long userId
    );

    TestResultResponse getResult(
            Long attemptId,
            Long userId
    );

    List<QuestionReviewResponse> getAnswerReview(
            Long attemptId,
            Long userId
    );

    List<AttemptHistoryResponse> getUserAttempts(
            Long userId
    );
}
