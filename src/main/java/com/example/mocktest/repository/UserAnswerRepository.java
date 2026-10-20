package com.example.mocktest.repository;


import com.example.mocktest.model.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository
        extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findByAttemptId(Long attemptId);

    Optional<UserAnswer> findByAttemptIdAndQuestionId(
            Long attemptId,
            Long questionId
    );

    boolean existsByAttemptIdAndQuestionId(
            Long attemptId,
            Long questionId
    );

    long countByAttemptId(Long attemptId);

    long countByAttemptIdAndCorrectTrue(Long attemptId);

    long countByAttemptIdAndCorrectFalse(Long attemptId);
}