package com.example.mocktest.repository;

import com.example.mocktest.model.MockTest;
import com.example.mocktest.model.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findByActiveTrue();

    boolean existsByTitle(String title);

    @Query("""
            select distinct test
            from MockTest test
            left join test.questions testQuestion
            left join testQuestion.question question
            left join question.subject subject
            where test.active = true
              and (:search is null
                   or lower(test.title) like lower(concat('%', :search, '%'))
                   or lower(test.description) like lower(concat('%', :search, '%')))
              and (:subjectId is null or subject.id = :subjectId)
              and (:sectionId is null or subject.section.id = :sectionId)
              and (:difficulty is null or question.difficulty = :difficulty)
              and (:durationType is null
                   or (:durationType = 'quick' and test.durationMinutes <= 30)
                   or (:durationType = 'full' and test.durationMinutes > 30))
            """)
    Page<MockTest> findActiveCatalog(
            @Param("search") String search,
            @Param("subjectId") Long subjectId,
            @Param("sectionId") Long sectionId,
            @Param("difficulty") Difficulty difficulty,
            @Param("durationType") String durationType,
            Pageable pageable
    );
}
