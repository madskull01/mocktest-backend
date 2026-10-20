package com.example.mocktest.service;


import com.example.mocktest.dto.subject.CreateSubjectRequest;
import com.example.mocktest.dto.subject.SubjectResponse;
import com.example.mocktest.dto.subject.UpdateSubjectRequest;
import com.example.mocktest.model.Subject;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(
            Long sectionId,
            CreateSubjectRequest request
    );

    SubjectResponse getSubjectById(Long id);

    List<SubjectResponse> getAllSubjects();

    List<SubjectResponse> getSubjectsBySection(
            Long sectionId
    );

    List<SubjectResponse> getActiveSubjectsBySection(
            Long sectionId
    );

    SubjectResponse updateSubject(
            Long id,
            UpdateSubjectRequest request
    );

    void deleteSubject(Long id);
}
