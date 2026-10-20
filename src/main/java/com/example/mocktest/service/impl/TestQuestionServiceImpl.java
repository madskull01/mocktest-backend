package com.example.mocktest.service.impl;

import com.example.mocktest.dto.mocktest.AddQuestionToTestRequest;
import com.example.mocktest.dto.mocktest.TestQuestionResponse;
import com.example.mocktest.model.MockTest;
import com.example.mocktest.model.Question;
import com.example.mocktest.model.TestQuestion;
import com.example.mocktest.repository.MockTestRepository;
import com.example.mocktest.repository.QuestionRepository;
import com.example.mocktest.repository.TestQuestionRepository;
import com.example.mocktest.service.TestQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestQuestionServiceImpl
        implements TestQuestionService {

    private final TestQuestionRepository testQuestionRepository;
    private final MockTestRepository mockTestRepository;
    private final QuestionRepository questionRepository;

    @Override
    public TestQuestionResponse addQuestionToTest(
            Long testId,
            AddQuestionToTestRequest request) {

        MockTest test = mockTestRepository.findById(testId)
                .orElseThrow(() ->
                        new RuntimeException("Test not found"));

        Question question = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found"));

        if (testQuestionRepository
                .existsByMockTestIdAndQuestionId(
                        testId,
                        question.getId())) {

            throw new RuntimeException(
                    "Question already exists in this test");
        }

        TestQuestion testQuestion =
                TestQuestion.builder()
                        .mockTest(test)
                        .question(question)
                        .questionOrder(
                                request.getQuestionOrder())
                        .marks(question.getMarks())
                        .build();

        return mapToResponse(
                testQuestionRepository.save(testQuestion)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestQuestionResponse> getQuestionsByTest(
            Long testId) {

        if (!mockTestRepository.existsById(testId)) {
            throw new RuntimeException("Test not found");
        }

        return testQuestionRepository
                .findByMockTestIdOrderByQuestionOrder(testId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void removeQuestionFromTest(
            Long testId,
            Long questionId) {

        TestQuestion testQuestion =
                testQuestionRepository
                        .findByMockTestIdAndQuestionId(
                                testId,
                                questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found in test"));

        testQuestionRepository.delete(testQuestion);
    }

    private TestQuestionResponse mapToResponse(
            TestQuestion testQuestion) {

        return TestQuestionResponse.builder()
                .id(testQuestion.getId())
                .questionId(
                        testQuestion.getQuestion().getId())
                .subjectId(testQuestion.getQuestion().getSubject().getId())
                .subjectName(testQuestion.getQuestion().getSubject().getName())
                .questionOrder(
                        testQuestion.getQuestionOrder())
                .marks(
                        testQuestion.getMarks())
                .build();
    }
}