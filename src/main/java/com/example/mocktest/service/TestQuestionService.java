package com.example.mocktest.service;


import com.example.mocktest.dto.mocktest.AddQuestionToTestRequest;
import com.example.mocktest.dto.mocktest.TestQuestionResponse;
import com.example.mocktest.model.TestQuestion;

import java.util.List;

public interface TestQuestionService {

    TestQuestionResponse addQuestionToTest(
            Long testId,
            AddQuestionToTestRequest request
    );

    List<TestQuestionResponse> getQuestionsByTest(
            Long testId
    );

    void removeQuestionFromTest(
            Long testId,
            Long questionId
    );
}