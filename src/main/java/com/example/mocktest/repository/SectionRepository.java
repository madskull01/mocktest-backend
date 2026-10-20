package com.example.mocktest.repository;


import com.example.mocktest.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByActiveTrue();

    boolean existsByName(String name);
}
