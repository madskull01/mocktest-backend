package com.example.mocktest.repository;

import com.example.mocktest.model.TestAttempt;
import com.example.mocktest.model.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository
        extends JpaRepository<TestAttempt, Long> {

    List<TestAttempt> findByUserId(Long userId);

    List<TestAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    List<TestAttempt> findByMockTestId(Long mockTestId);

    List<TestAttempt> findByUserIdAndMockTestId(
            Long userId,
            Long mockTestId
    );

    Optional<TestAttempt> findByIdAndUserId(
            Long attemptId,
            Long userId
    );

    List<TestAttempt> findByUserIdAndStatus(
            Long userId,
            AttemptStatus status
    );
}
