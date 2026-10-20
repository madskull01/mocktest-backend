package com.example.mocktest.service.impl;

import com.example.mocktest.model.MockTest;
import com.example.mocktest.model.Question;
import com.example.mocktest.model.Section;
import com.example.mocktest.model.Subject;
import com.example.mocktest.model.TestAttempt;
import com.example.mocktest.model.TestQuestion;
import com.example.mocktest.model.User;
import com.example.mocktest.model.UserAnswer;
import com.example.mocktest.model.enums.AttemptStatus;
import com.example.mocktest.exception.ForbiddenException;
import com.example.mocktest.repository.MockTestRepository;
import com.example.mocktest.repository.TestAttemptRepository;
import com.example.mocktest.repository.TestQuestionRepository;
import com.example.mocktest.repository.UserAnswerRepository;
import com.example.mocktest.repository.UserRepository;
import com.example.mocktest.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestAttemptServiceImplTest {

    private TestAttemptRepository attemptRepository;
    private UserAnswerRepository userAnswerRepository;
    private UserRepository userRepository;
    private MockTestRepository mockTestRepository;
    private TestQuestionRepository testQuestionRepository;
    private PaymentService paymentService;
    private TestAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        attemptRepository = mock(TestAttemptRepository.class);
        userAnswerRepository = mock(UserAnswerRepository.class);
        userRepository = mock(UserRepository.class);
        mockTestRepository = mock(MockTestRepository.class);
        testQuestionRepository = mock(TestQuestionRepository.class);
        paymentService = mock(PaymentService.class);
        service = new TestAttemptServiceImpl(
                attemptRepository,
                userRepository,
                mockTestRepository,
                testQuestionRepository,
                userAnswerRepository,
                paymentService
        );
    }

    @Test
    void freeTestCanStartWithoutPurchasedSectionAccess() {
        User user = User.builder().id(3L).build();
        MockTest test = MockTest.builder()
                .id(12L)
                .title("Demo")
                .active(true)
                .freeAccess(true)
                .build();
        Section section = Section.builder().id(2L).name("Section A").build();
        Subject subject = Subject.builder().section(section).build();
        Question question = Question.builder().subject(subject).build();
        TestQuestion testQuestion = TestQuestion.builder()
                .question(question)
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(mockTestRepository.findById(12L)).thenReturn(Optional.of(test));
        when(testQuestionRepository.findByMockTestIdOrderByQuestionOrder(12L))
                .thenReturn(java.util.List.of(testQuestion));
        when(attemptRepository.save(any(TestAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.startTest(3L, 12L);

        verify(paymentService, never()).hasSectionAccess(3L, section);
    }

    @Test
    void paidTestStillRequiresPurchasedSectionAccess() {
        User user = User.builder().id(3L).build();
        MockTest test = MockTest.builder()
                .id(12L)
                .title("Paid test")
                .active(true)
                .build();
        Section section = Section.builder().id(2L).name("Section A").build();
        Subject subject = Subject.builder().section(section).build();
        Question question = Question.builder().subject(subject).build();
        TestQuestion testQuestion = TestQuestion.builder()
                .question(question)
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(mockTestRepository.findById(12L)).thenReturn(Optional.of(test));
        when(testQuestionRepository.findByMockTestIdOrderByQuestionOrder(12L))
                .thenReturn(java.util.List.of(testQuestion));
        when(paymentService.hasSectionAccess(3L, section)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.startTest(3L, 12L));
    }

    @Test
    void submittingCompletedAttemptReturnsExistingResultWithoutRescoring() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        MockTest test = MockTest.builder()
                .id(12L)
                .title("Practice")
                .passingMarks(5)
                .build();
        TestAttempt attempt = TestAttempt.builder()
                .id(7L)
                .mockTest(test)
                .startedAt(startedAt)
                .submittedAt(startedAt.plusMinutes(4))
                .status(AttemptStatus.COMPLETED)
                .score(8)
                .totalMarks(10)
                .correctAnswers(4)
                .incorrectAnswers(1)
                .unanswered(0)
                .build();
        when(attemptRepository.findByIdAndUserId(7L, 3L))
                .thenReturn(Optional.of(attempt));

        var result = service.submitTest(7L, 3L);

        assertEquals(8, result.getScore());
        assertEquals(5, result.getTotalQuestions());
        verify(userAnswerRepository, never()).findByAttemptId(7L);
    }

    @Test
    void submittingIncorrectAnswerWithMissingLegacyNegativeMarksCompletesAttempt() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(2);
        MockTest test = MockTest.builder()
                .id(12L)
                .title("Practice")
                .passingMarks(1)
                .build();
        TestAttempt attempt = TestAttempt.builder()
                .id(7L)
                .mockTest(test)
                .startedAt(startedAt)
                .totalMarks(1)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        Question question = Question.builder()
                .marks(1)
                .negativeMarks(null)
                .build();
        UserAnswer incorrectAnswer = UserAnswer.builder()
                .question(question)
                .correct(false)
                .build();
        when(attemptRepository.findByIdAndUserId(7L, 3L))
                .thenReturn(Optional.of(attempt));
        when(userAnswerRepository.findByAttemptId(7L))
                .thenReturn(List.of(incorrectAnswer));
        when(testQuestionRepository.countByMockTestId(12L))
                .thenReturn(1L);
        when(attemptRepository.save(attempt)).thenReturn(attempt);

        var result = service.submitTest(7L, 3L);

        assertEquals(0, result.getScore());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
        assertEquals(AttemptStatus.COMPLETED, attempt.getStatus());
    }

    @Test
    void answerReviewRequiresTheOwningStudentsCompletedAttempt() {
        when(attemptRepository.findByIdAndUserId(7L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.getAnswerReview(7L, 99L)
        );

        TestAttempt inProgress = TestAttempt.builder()
                .id(7L)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        when(attemptRepository.findByIdAndUserId(7L, 3L))
                .thenReturn(Optional.of(inProgress));

        assertThrows(
                RuntimeException.class,
                () -> service.getAnswerReview(7L, 3L)
        );
    }
}
