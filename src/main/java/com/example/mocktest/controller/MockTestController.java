package com.example.mocktest.controller;

import com.example.mocktest.dto.mocktest.*;
import com.example.mocktest.exception.BadRequestException;
import com.example.mocktest.model.enums.Difficulty;
import com.example.mocktest.service.MockTestService;
import com.example.mocktest.service.TestQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-tests")
@RequiredArgsConstructor
public class MockTestController {

    private final MockTestService mockTestService;
    private final TestQuestionService testQuestionService;

    @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MockTestResponse> createTest(
            @Valid @RequestBody CreateMockTestRequest request) {

        MockTestResponse response =
                mockTestService.createTest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MockTestResponse>>
    getAllTests() {

        return ResponseEntity.ok(
                mockTestService.getAllTests()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<MockTestResponse>>
    getActiveTests() {

        return ResponseEntity.ok(
                mockTestService.getActiveTests()
        );
    }

    @GetMapping("/catalog")
    public ResponseEntity<Page<MockTestResponse>> getActiveTestCatalog(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "all") String duration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        if (page < 0 || size < 1 || size > 50) {
            throw new BadRequestException(
                    "Page must be non-negative and size must be between 1 and 50");
        }

        if (subjectId != null && subjectId < 1
                || sectionId != null && sectionId < 1) {
            throw new BadRequestException(
                    "Subject and section IDs must be positive");
        }

        if (!List.of("all", "quick", "full").contains(duration)) {
            throw new BadRequestException(
                    "Duration must be all, quick, or full");
        }

        if (!List.of("title", "createdAt", "durationMinutes", "totalMarks")
                .contains(sortBy)) {
            throw new BadRequestException("Unsupported sort field");
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Direction must be asc or desc");
        }

        return ResponseEntity.ok(mockTestService.getActiveTestCatalog(
                search,
                subjectId,
                sectionId,
                difficulty,
                "all".equals(duration) ? null : duration,
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy))));
    }

    @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MockTestResponse>
    getTestById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                mockTestService.getTestById(id)
        );
    }

    @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MockTestResponse>
    updateTest(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMockTestRequest request) {

        return ResponseEntity.ok(
                mockTestService.updateTest(id, request)
        );
    }

    @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTest(
            @PathVariable Long id) {

        mockTestService.deleteTest(id);

        return ResponseEntity.noContent().build();
    }

    // -------------------------------
    // Questions inside a mock test
    // -------------------------------

    @PostMapping("/{testId}/questions")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestQuestionResponse>
    addQuestionToTest(
            @PathVariable Long testId,
            @Valid @RequestBody AddQuestionToTestRequest request) {

        TestQuestionResponse response =
                testQuestionService.addQuestionToTest(
                        testId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{testId}/questions")
    public ResponseEntity<List<TestQuestionResponse>>
    getQuestionsByTest(
            @PathVariable Long testId) {

        return ResponseEntity.ok(
                testQuestionService.getQuestionsByTest(
                        testId
                )
        );
    }

    @DeleteMapping("/{testId}/questions/{questionId}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void>
    removeQuestionFromTest(
            @PathVariable Long testId,
            @PathVariable Long questionId) {

        testQuestionService.removeQuestionFromTest(
                testId,
                questionId
        );

        return ResponseEntity.noContent().build();
    }
}
