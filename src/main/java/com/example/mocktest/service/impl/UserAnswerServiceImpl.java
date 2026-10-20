package com.example.mocktest.service.impl;

import com.example.mocktest.dto.attempt.StudentAnswerResponse;
import com.example.mocktest.dto.attempt.SubmitAnswerRequest;
import com.example.mocktest.model.Question;
import com.example.mocktest.model.QuestionOption;
import com.example.mocktest.model.TestAttempt;
import com.example.mocktest.model.UserAnswer;
import com.example.mocktest.model.enums.AttemptStatus;
import com.example.mocktest.repository.*;
import com.example.mocktest.service.UserAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAnswerServiceImpl
        implements UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final TestQuestionRepository testQuestionRepository;

    @Override
    public StudentAnswerResponse saveAnswer(
            Long attemptId,
            Long userId,
            SubmitAnswerRequest request) {

        TestAttempt attempt =
                testAttemptRepository
                        .findByIdAndUserId(
                                attemptId,
                                userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attempt not found"));

        if (attempt.getStatus()
                != AttemptStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Test has already been submitted");
        }

        LocalDateTime expiresAt = attempt.getStartedAt()
                .plusMinutes(attempt.getMockTest().getDurationMinutes());
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new RuntimeException("Test time has expired");
        }

        Question question =
                questionRepository
                        .findById(request.getQuestionId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"));

        boolean belongsToTest =
                testQuestionRepository
                        .existsByMockTestIdAndQuestionId(
                                attempt.getMockTest().getId(),
                                question.getId());

        if (!belongsToTest) {

            throw new RuntimeException(
                    "Question does not belong to this test");
        }

        QuestionOption option =
                optionRepository
                        .findById(request.getOptionId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Option not found"));

        if (!option.getQuestion().getId()
                .equals(question.getId())) {

            throw new RuntimeException(
                    "Option does not belong to this question");
        }

        UserAnswer answer =
                userAnswerRepository
                        .findByAttemptIdAndQuestionId(
                                attemptId,
                                question.getId())
                        .orElse(
                                UserAnswer.builder()
                                        .attempt(attempt)
                                        .question(question)
                                        .build()
                        );

        answer.setSelectedOption(option);
        answer.setCorrect(option.isCorrect());

        answer.setMarksObtained(
                option.isCorrect()
                        ? question.getMarks()
                        : 0
        );

        answer.setAnsweredAt(
                LocalDateTime.now()
        );

        userAnswerRepository.save(answer);

        return StudentAnswerResponse.builder()
                .questionId(question.getId())
                .selectedOptionId(option.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAnswerResponse> getAnswersByAttempt(
            Long attemptId,
            Long userId) {

        testAttemptRepository
                .findByIdAndUserId(attemptId, userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Attempt not found"));

        return userAnswerRepository
                .findByAttemptId(attemptId)
                .stream()
                .map(answer ->
                        StudentAnswerResponse.builder()
                                .questionId(
                                        answer.getQuestion()
                                                .getId())
                                .selectedOptionId(
                                        answer.getSelectedOption()
                                                != null
                                                ? answer.getSelectedOption()
                                                .getId()
                                                : null)
                                .build())
                .toList();
    }
}