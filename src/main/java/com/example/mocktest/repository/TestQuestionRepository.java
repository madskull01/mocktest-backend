package com.example.mocktest.repository;


import com.example.mocktest.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestQuestionRepository
        extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByMockTestIdOrderByQuestionOrder(Long mockTestId);

    Optional<TestQuestion> findByMockTestIdAndQuestionId(
            Long mockTestId,
            Long questionId
    );

    boolean existsByMockTestIdAndQuestionId(
            Long mockTestId,
            Long questionId
    );

    long countByMockTestId(Long mockTestId);
}
