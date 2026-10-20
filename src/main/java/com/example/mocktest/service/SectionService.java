package com.example.mocktest.service;


import com.example.mocktest.dto.section.CreateSectionRequest;
import com.example.mocktest.dto.section.SectionResponse;
import com.example.mocktest.dto.section.UpdateSectionRequest;
import com.example.mocktest.model.Section;

import java.util.List;

public interface SectionService {

    SectionResponse createSection(
            CreateSectionRequest request
    );

    SectionResponse getSectionById(Long id);

    List<SectionResponse> getAllSections();

    List<SectionResponse> getActiveSections();

    SectionResponse updateSection(
            Long id,
            UpdateSectionRequest request
    );

    void deleteSection(Long id);
}