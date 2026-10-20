package com.example.mocktest.controller;

import com.example.mocktest.dto.attempt.*;
import com.example.mocktest.dto.mocktest.StudentTestResponse;
import com.example.mocktest.service.TestAttemptService;
import com.example.mocktest.service.UserAnswerService;
import com.example.mocktest.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;
    private final UserAnswerService userAnswerService;

    @PostMapping("/start/{testId}")
    public ResponseEntity<StartTestResponse> startTest(
            @PathVariable Long testId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.startTest(
                        user.userId(),
                        testId
                )
        );
    }

    @GetMapping("/{attemptId}/test")
    public ResponseEntity<StudentTestResponse>
    getTestForAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.getTestForAttempt(
                        attemptId,
                        user.userId()
                )
        );
    }

    @PostMapping("/{attemptId}/answers")
    public ResponseEntity<StudentAnswerResponse>
    submitAnswer(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SubmitAnswerRequest request) {

        return ResponseEntity.ok(
                userAnswerService.saveAnswer(
                        attemptId,
                        user.userId(),
                        request
                )
        );
    }

    @GetMapping("/{attemptId}/answers")
    public ResponseEntity<List<StudentAnswerResponse>>
    getAnswers(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                userAnswerService.getAnswersByAttempt(
                        attemptId,
                        user.userId()
                )
        );
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<TestResultResponse>
    submitTest(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.submitTest(
                        attemptId,
                        user.userId()
                )
        );
    }

    @GetMapping("/{attemptId}/result")
    public ResponseEntity<TestResultResponse>
    getResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.getResult(
                        attemptId,
                        user.userId()
                )
        );
    }

    @GetMapping("/{attemptId}/review")
    public ResponseEntity<List<QuestionReviewResponse>> getAnswerReview(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.getAnswerReview(
                        attemptId,
                        user.userId())
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttemptHistoryResponse>>
    getAttemptHistory(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                testAttemptService.getUserAttempts(
                        user.userId()
                )
        );
    }
}
