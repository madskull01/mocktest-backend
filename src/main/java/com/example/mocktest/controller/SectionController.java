package com.example.mocktest.controller;

import com.example.mocktest.dto.section.CreateSectionRequest;
import com.example.mocktest.dto.section.SectionResponse;
import com.example.mocktest.dto.section.UpdateSectionRequest;
import com.example.mocktest.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> createSection(
            @Valid @RequestBody CreateSectionRequest request) {

        SectionResponse response =
                sectionService.createSection(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SectionResponse>> getAllSections() {

        return ResponseEntity.ok(
                sectionService.getAllSections()
        );
    }

    @GetMapping("/active")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SectionResponse>>
    getActiveSections() {

        return ResponseEntity.ok(
                sectionService.getActiveSections()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> getSectionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sectionService.getSectionById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSectionRequest request) {

        return ResponseEntity.ok(
                sectionService.updateSection(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSection(
            @PathVariable Long id) {

        sectionService.deleteSection(id);

        return ResponseEntity.noContent().build();
    }
}
