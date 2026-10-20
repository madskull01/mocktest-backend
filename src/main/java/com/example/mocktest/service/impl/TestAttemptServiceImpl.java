package com.example.mocktest.service.impl;

import com.example.mocktest.dto.attempt.AttemptHistoryResponse;
import com.example.mocktest.dto.attempt.AnswerOptionReviewResponse;
import com.example.mocktest.dto.attempt.QuestionReviewResponse;
import com.example.mocktest.dto.attempt.StartTestResponse;
import com.example.mocktest.dto.attempt.TestResultResponse;
import com.example.mocktest.dto.mocktest.StudentTestResponse;
import com.example.mocktest.dto.question.StudentQuestionOptionResponse;
import com.example.mocktest.dto.question.StudentQuestionResponse;
import com.example.mocktest.model.*;
import com.example.mocktest.model.enums.AttemptStatus;
import com.example.mocktest.repository.*;
import com.example.mocktest.service.TestAttemptService;
import com.example.mocktest.service.payment.PaidSection;
import com.example.mocktest.service.payment.PaymentService;
import com.example.mocktest.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestAttemptServiceImpl
        implements TestAttemptService {

    private final TestAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final MockTestRepository mockTestRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final PaymentService paymentService;

    @Override
    public StartTestResponse startTest(
            Long userId,
            Long testId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        MockTest test = mockTestRepository.findById(testId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock test not found"));

        if (!test.isActive()) {

            throw new RuntimeException(
                    "This test is not active");
        }

        List<TestQuestion> testQuestions =
                testQuestionRepository
                        .findByMockTestIdOrderByQuestionOrder(testId);
        long questionCount = testQuestions.size();

        if (questionCount == 0) {

            throw new RuntimeException(
                    "Test does not contain any questions");
        }

        if (!test.isFreeAccess()) {
            for (TestQuestion testQuestion : testQuestions) {
                Section section = testQuestion.getQuestion()
                        .getSubject().getSection();
                if (!paymentService.hasSectionAccess(userId, section)) {
                    PaidSection paidSection = PaidSection.fromName(section.getName());
                    throw new ForbiddenException(
                            "Purchase Section " + paidSection
                                    + " access before starting this test");
                }
            }
        }

        LocalDateTime startedAt =
                LocalDateTime.now();

        TestAttempt attempt =
                TestAttempt.builder()
                        .user(user)
                        .mockTest(test)
                        .startedAt(startedAt)
                        .totalMarks(
                                test.getTotalMarks())
                        .status(
                                AttemptStatus.IN_PROGRESS)
                        .build();

        TestAttempt saved =
                attemptRepository.save(attempt);

        return StartTestResponse.builder()
                .attemptId(saved.getId())
                .testId(test.getId())
                .testTitle(test.getTitle())
                .startedAt(saved.getStartedAt())
                .durationMinutes(
                        test.getDurationMinutes())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestResponse getTestForAttempt(
            Long attemptId,
            Long userId) {

        TestAttempt attempt =
                attemptRepository
                        .findByIdAndUserId(
                                attemptId,
                                userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attempt not found"));

        MockTest test =
                attempt.getMockTest();

        List<StudentQuestionResponse> questions =
                testQuestionRepository
                        .findByMockTestIdOrderByQuestionOrder(
                                test.getId())
                        .stream()
                        .map(testQuestion ->
                                mapToStudentQuestion(
                                        testQuestion.getQuestion()))
                        .toList();

        return StudentTestResponse.builder()
                .testId(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .durationMinutes(
                        test.getDurationMinutes())
                .totalMarks(
                        test.getTotalMarks())
                .totalQuestions(
                        questions.size())
                .questions(questions)
                .build();
    }

    @Override
    public TestResultResponse submitTest(
            Long attemptId,
            Long userId) {

        TestAttempt attempt =
                attemptRepository
                        .findByIdAndUserId(
                                attemptId,
                                userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attempt not found"));

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            return mapToResult(attempt);
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Test cannot be submitted");
        }

        List<UserAnswer> answers =
                userAnswerRepository
                        .findByAttemptId(attemptId);

        int score = 0;
        int correct = 0;
        int incorrect = 0;

        for (UserAnswer answer : answers) {

            Question question =
                    answer.getQuestion();

            if (Boolean.TRUE.equals(
                    answer.getCorrect())) {

                score += question.getMarks() == null
                        ? 0
                        : question.getMarks();
                correct++;

            } else {

                score -= question.getNegativeMarks() == null
                        ? 0
                        : question.getNegativeMarks();
                incorrect++;
            }
        }

        int totalQuestions =
                (int) testQuestionRepository
                        .countByMockTestId(
                                attempt.getMockTest().getId());

        int unanswered =
                totalQuestions - answers.size();

        attempt.setScore(score);
        attempt.setCorrectAnswers(correct);
        attempt.setIncorrectAnswers(incorrect);
        attempt.setUnanswered(unanswered);
        attempt.setSubmittedAt(
                LocalDateTime.now());
        attempt.setStatus(
                AttemptStatus.COMPLETED);

        TestAttempt saved =
                attemptRepository.save(attempt);

        return mapToResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultResponse getResult(
            Long attemptId,
            Long userId) {

        TestAttempt attempt =
                attemptRepository
                        .findByIdAndUserId(
                                attemptId,
                                userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {

            throw new RuntimeException(
                    "Test has not been completed");
        }

        return mapToResult(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionReviewResponse> getAnswerReview(
            Long attemptId,
            Long userId) {

        TestAttempt attempt = attemptRepository
                .findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new RuntimeException("Test has not been completed");
        }

        var savedAnswers = userAnswerRepository.findByAttemptId(attemptId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer));

        List<TestQuestion> testQuestions =
                testQuestionRepository.findByMockTestIdOrderByQuestionOrder(
                        attempt.getMockTest().getId());

        return java.util.stream.IntStream.range(0, testQuestions.size())
                .mapToObj(index -> {
                    Question question = testQuestions.get(index).getQuestion();
                    UserAnswer answer = savedAnswers.get(question.getId());
                    Long selectedOptionId = answer == null
                            || answer.getSelectedOption() == null
                            ? null
                            : answer.getSelectedOption().getId();
                    QuestionOption correctOption = question.getOptions()
                            .stream()
                            .filter(QuestionOption::isCorrect)
                            .findFirst()
                            .orElse(null);
                    boolean isCorrect = answer != null
                            && Boolean.TRUE.equals(answer.getCorrect());
                    boolean answered = selectedOptionId != null;
                    int marksAwarded = isCorrect
                            ? question.getMarks()
                            : 0;
                    int marksDeducted = answered && !isCorrect
                            ? question.getNegativeMarks()
                            : 0;

                    List<AnswerOptionReviewResponse> options =
                            question.getOptions().stream()
                                    .map(option -> AnswerOptionReviewResponse.builder()
                                            .optionId(option.getId())
                                            .optionText(option.getOptionText())
                                            .correct(option.isCorrect())
                                            .selected(option.getId().equals(selectedOptionId))
                                            .build())
                                    .toList();

                    return QuestionReviewResponse.builder()
                            .questionId(question.getId())
                            .questionNumber(index + 1)
                            .questionText(question.getQuestionText())
                            .imageUrl(question.getImageUrl())
                            .options(options)
                            .selectedOptionId(selectedOptionId)
                            .correctOptionId(correctOption == null
                                    ? null
                                    : correctOption.getId())
                            .explanation(question.getExplanation())
                            .answered(answered)
                            .correct(isCorrect)
                            .marksAwarded(marksAwarded)
                            .marksDeducted(marksDeducted)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttemptHistoryResponse> getUserAttempts(
            Long userId) {

        if (!userRepository.existsById(userId)) {

            throw new RuntimeException(
                    "User not found");
        }

        return attemptRepository
                .findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::mapToHistory)
                .toList();
    }

    private StudentQuestionResponse mapToStudentQuestion(
            Question question) {

        List<StudentQuestionOptionResponse> options =
                question.getOptions()
                        .stream()
                        .map(option ->
                                StudentQuestionOptionResponse
                                        .builder()
                                        .id(option.getId())
                                        .optionText(
                                                option.getOptionText())
                                        .build())
                        .toList();

        return StudentQuestionResponse.builder()
                .id(question.getId())
                .questionText(
                        question.getQuestionText())
                .imageUrl(question.getImageUrl())
                .marks(question.getMarks())
                .difficulty(
                        question.getDifficulty())
                .options(options)
                .build();
    }

    private TestResultResponse mapToResult(
            TestAttempt attempt) {

        int totalMarks =
                attempt.getTotalMarks();

        double percentage =
                totalMarks > 0
                        ? ((double) attempt.getScore()
                           / totalMarks) * 100
                        : 0;

        boolean passed =
                attempt.getScore()
                        >= attempt.getMockTest()
                        .getPassingMarks();

        return TestResultResponse.builder()
                .attemptId(attempt.getId())
                .testId(
                        attempt.getMockTest().getId())
                .testTitle(
                        attempt.getMockTest().getTitle())
                .score(attempt.getScore())
                .totalMarks(totalMarks)
                .correctAnswers(
                        attempt.getCorrectAnswers())
                .incorrectAnswers(
                        attempt.getIncorrectAnswers())
                .unanswered(
                        attempt.getUnanswered())
                .totalQuestions(
                        attempt.getCorrectAnswers()
                                + attempt.getIncorrectAnswers()
                                + attempt.getUnanswered())
                .attemptedQuestions(
                        attempt.getCorrectAnswers()
                                + attempt.getIncorrectAnswers())
                .timeTakenSeconds(
                        java.time.Duration.between(
                                attempt.getStartedAt(),
                                attempt.getSubmittedAt())
                                .getSeconds())
                .percentage(percentage)
                .passed(passed)
                .startedAt(
                        attempt.getStartedAt())
                .submittedAt(
                        attempt.getSubmittedAt())
                .build();
    }

    private AttemptHistoryResponse mapToHistory(
            TestAttempt attempt) {

        int totalMarks =
                attempt.getTotalMarks();

        double percentage =
                totalMarks > 0
                        ? ((double) attempt.getScore()
                           / totalMarks) * 100
                        : 0;

        return AttemptHistoryResponse.builder()
                .attemptId(attempt.getId())
                .testId(
                        attempt.getMockTest().getId())
                .testTitle(
                        attempt.getMockTest().getTitle())
                .score(attempt.getScore())
                .totalMarks(totalMarks)
                .percentage(percentage)
                .status(attempt.getStatus())
                .startedAt(
                        attempt.getStartedAt())
                .submittedAt(
                        attempt.getSubmittedAt())
                .build();
    }
}