package com.example.mocktest.repository;

import com.example.mocktest.model.Question;
import com.example.mocktest.model.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubjectId(Long subjectId);

    List<Question> findBySubjectIdAndActiveTrue(Long subjectId);

    List<Question> findByDifficulty(Difficulty difficulty);

    List<Question> findBySubjectIdAndDifficulty(
            Long subjectId,
            Difficulty difficulty
    );

    List<Question> findByActiveTrue();
}
