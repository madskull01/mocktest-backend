package com.example.mocktest.service.impl;

import com.example.mocktest.dto.mocktest.CreateMockTestRequest;
import com.example.mocktest.dto.mocktest.MockTestResponse;
import com.example.mocktest.dto.mocktest.UpdateMockTestRequest;
import com.example.mocktest.model.MockTest;
import com.example.mocktest.repository.MockTestRepository;
import com.example.mocktest.repository.TestQuestionRepository;
import com.example.mocktest.service.MockTestService;
import com.example.mocktest.model.enums.Difficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MockTestServiceImpl
        implements MockTestService {

    private final MockTestRepository mockTestRepository;
    private final TestQuestionRepository testQuestionRepository;

    @Override
    public MockTestResponse createTest(
            CreateMockTestRequest request) {

        if (mockTestRepository.existsByTitle(
                request.getTitle())) {

            throw new RuntimeException(
                    "Test with this title already exists");
        }

        if (request.getPassingMarks()
                > request.getTotalMarks()) {

            throw new RuntimeException(
                    "Passing marks cannot exceed total marks");
        }

        MockTest test = MockTest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(
                        request.getDurationMinutes())
                .totalMarks(
                        request.getTotalMarks())
                .passingMarks(
                        request.getPassingMarks())
                .freeAccess(request.isFreeAccess())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapToResponse(
                mockTestRepository.save(test)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MockTestResponse getTestById(Long id) {

        return mapToResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MockTestResponse> getAllTests() {

        return mockTestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MockTestResponse> getActiveTests() {

        return mockTestRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MockTestResponse> getActiveTestCatalog(
            String search,
            Long subjectId,
            Long sectionId,
            Difficulty difficulty,
            String durationType,
            Pageable pageable) {

        String normalizedSearch = search == null || search.isBlank()
                ? null
                : search.trim();

        return mockTestRepository.findActiveCatalog(
                        normalizedSearch,
                        subjectId,
                        sectionId,
                        difficulty,
                        durationType,
                        pageable)
                .map(this::mapToResponse);
    }

    @Override
    public MockTestResponse updateTest(
            Long id,
            UpdateMockTestRequest request) {

        MockTest test = getEntityById(id);

        if (request.getPassingMarks()
                > request.getTotalMarks()) {

            throw new RuntimeException(
                    "Passing marks cannot exceed total marks");
        }

        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setDurationMinutes(
                request.getDurationMinutes());
        test.setTotalMarks(
                request.getTotalMarks());
        test.setPassingMarks(
                request.getPassingMarks());
        if (request.getFreeAccess() != null) {
            test.setFreeAccess(request.getFreeAccess());
        }
        test.setActive(request.isActive());
        test.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(
                mockTestRepository.save(test)
        );
    }

    @Override
    public void deleteTest(Long id) {

        MockTest test = getEntityById(id);

        mockTestRepository.delete(test);
    }

    private MockTest getEntityById(Long id) {

        return mockTestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock test not found"));
    }

    private MockTestResponse mapToResponse(
            MockTest test) {

        int questionCount =
                (int) testQuestionRepository
                        .countByMockTestId(test.getId());

        return MockTestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .durationMinutes(
                        test.getDurationMinutes())
                .totalMarks(
                        test.getTotalMarks())
                .passingMarks(
                        test.getPassingMarks())
                .freeAccess(test.isFreeAccess())
                .active(test.isActive())
                .questionCount(questionCount)
                .createdAt(test.getCreatedAt())
                .build();
    }
}