package com.example.mocktest.repository;

import com.example.mocktest.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySectionId(Long sectionId);

    List<Subject> findBySectionIdAndActiveTrue(Long sectionId);

    boolean existsByNameAndSectionId(String name, Long sectionId);
}
