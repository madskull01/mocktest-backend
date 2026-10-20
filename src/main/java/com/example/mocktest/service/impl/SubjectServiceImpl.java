package com.example.mocktest.service.impl;

import com.example.mocktest.dto.subject.CreateSubjectRequest;
import com.example.mocktest.dto.subject.SubjectResponse;
import com.example.mocktest.dto.subject.UpdateSubjectRequest;
import com.example.mocktest.model.Section;
import com.example.mocktest.model.Subject;
import com.example.mocktest.repository.SectionRepository;
import com.example.mocktest.repository.SubjectRepository;
import com.example.mocktest.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;

    @Override
    public SubjectResponse createSubject(
            Long sectionId,
            CreateSubjectRequest request) {

        Section section = getSection(sectionId);

        if (subjectRepository.existsByNameAndSectionId(
                request.getName(),
                sectionId)) {

            throw new RuntimeException(
                    "Subject already exists in this section");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .section(section)
                .build();

        return mapToResponse(
                subjectRepository.save(subject)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {

        return mapToResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {

        return subjectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsBySection(
            Long sectionId) {

        getSection(sectionId);

        return subjectRepository.findBySectionId(sectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getActiveSubjectsBySection(
            Long sectionId) {

        getSection(sectionId);

        return subjectRepository
                .findBySectionIdAndActiveTrue(sectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SubjectResponse updateSubject(
            Long id,
            UpdateSubjectRequest request) {

        Subject subject = getEntityById(id);

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());
        subject.setActive(request.isActive());

        return mapToResponse(
                subjectRepository.save(subject)
        );
    }

    @Override
    public void deleteSubject(Long id) {

        Subject subject = getEntityById(id);

        subjectRepository.delete(subject);
    }

    private Subject getEntityById(Long id) {

        return subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));
    }

    private Section getSection(Long id) {

        return sectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));
    }

    private SubjectResponse mapToResponse(Subject subject) {

        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .description(subject.getDescription())
                .active(subject.isActive())
                .sectionId(subject.getSection().getId())
                .sectionName(subject.getSection().getName())
                .build();
    }
}