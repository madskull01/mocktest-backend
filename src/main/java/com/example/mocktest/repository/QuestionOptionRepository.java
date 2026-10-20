package com.example.mocktest.repository;

import com.example.mocktest.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionId(Long questionId);

    long countByQuestionId(Long questionId);

    long countByQuestionIdAndCorrectTrue(Long questionId);
}
