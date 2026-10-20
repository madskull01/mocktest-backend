package com.example.mocktest.service;


import com.example.mocktest.dto.mocktest.CreateMockTestRequest;
import com.example.mocktest.dto.mocktest.MockTestResponse;
import com.example.mocktest.dto.mocktest.UpdateMockTestRequest;
import com.example.mocktest.model.enums.Difficulty;
import com.example.mocktest.model.MockTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MockTestService {

    MockTestResponse createTest(
            CreateMockTestRequest request
    );

    MockTestResponse getTestById(Long id);

    List<MockTestResponse> getAllTests();

    List<MockTestResponse> getActiveTests();

    Page<MockTestResponse> getActiveTestCatalog(
            String search,
            Long subjectId,
            Long sectionId,
            Difficulty difficulty,
            String durationType,
            Pageable pageable
    );

    MockTestResponse updateTest(
            Long id,
            UpdateMockTestRequest request
    );

    void deleteTest(Long id);
}
