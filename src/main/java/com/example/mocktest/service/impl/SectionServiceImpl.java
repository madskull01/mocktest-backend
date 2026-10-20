package com.example.mocktest.service.impl;

import com.example.mocktest.dto.section.CreateSectionRequest;
import com.example.mocktest.dto.section.SectionResponse;
import com.example.mocktest.dto.section.UpdateSectionRequest;
import com.example.mocktest.model.Section;
import com.example.mocktest.repository.SectionRepository;
import com.example.mocktest.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;

    @Override
    public SectionResponse createSection(
            CreateSectionRequest request) {

        if (sectionRepository.existsByName(request.getName())) {
            throw new RuntimeException(
                    "Section already exists");
        }

        Section section = Section.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        return mapToResponse(
                sectionRepository.save(section)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponse getSectionById(Long id) {

        return mapToResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> getAllSections() {

        return sectionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> getActiveSections() {

        return sectionRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SectionResponse updateSection(
            Long id,
            UpdateSectionRequest request) {

        Section section = getEntityById(id);

        section.setName(request.getName());
        section.setDescription(request.getDescription());
        section.setActive(request.isActive());

        return mapToResponse(
                sectionRepository.save(section)
        );
    }

    @Override
    public void deleteSection(Long id) {

        Section section = getEntityById(id);

        sectionRepository.delete(section);
    }

    private Section getEntityById(Long id) {

        return sectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));
    }

    private SectionResponse mapToResponse(Section section) {

        return SectionResponse.builder()
                .id(section.getId())
                .name(section.getName())
                .description(section.getDescription())
                .active(section.isActive())
                .build();
    }
}