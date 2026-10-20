package com.example.mocktest.controller;

import com.example.mocktest.dto.subject.CreateSubjectRequest;
import com.example.mocktest.dto.subject.SubjectResponse;
import com.example.mocktest.dto.subject.UpdateSubjectRequest;
import com.example.mocktest.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/sections/{sectionId}/subjects")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> createSubject(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateSubjectRequest request) {

        SubjectResponse response =
                subjectService.createSubject(
                        sectionId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponse>>
    getAllSubjects() {

        return ResponseEntity.ok(
                subjectService.getAllSubjects()
        );
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                subjectService.getSubjectById(id)
        );
    }

    @GetMapping("/sections/{sectionId}/subjects")
    public ResponseEntity<List<SubjectResponse>>
    getSubjectsBySection(
            @PathVariable Long sectionId) {

        return ResponseEntity.ok(
                subjectService.getSubjectsBySection(
                        sectionId
                )
        );
    }

    @GetMapping("/sections/{sectionId}/subjects/active")
    public ResponseEntity<List<SubjectResponse>>
    getActiveSubjectsBySection(
            @PathVariable Long sectionId) {

        return ResponseEntity.ok(
                subjectService.getActiveSubjectsBySection(
                        sectionId
                )
        );
    }

    @PutMapping("/subjects/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        return ResponseEntity.ok(
                subjectService.updateSubject(id, request)
        );
    }

    @DeleteMapping("/subjects/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.noContent().build();
    }
}
