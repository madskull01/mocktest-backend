package com.example.mocktest.service;

import com.example.mocktest.model.Section;
import com.example.mocktest.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SectionGroupInitializerTest {

    private SectionRepository sectionRepository;
    private SectionGroupInitializer initializer;

    @BeforeEach
    void setUp() {
        sectionRepository = mock(SectionRepository.class);
        initializer = new SectionGroupInitializer(sectionRepository);
    }

    @Test
    void migratesAptitudeToSectionAAndCreatesSectionsBAndC() {
        Section aptitude = Section.builder()
                .id(1L)
                .name("Aptitude")
                .active(true)
                .build();
        when(sectionRepository.findAll()).thenReturn(List.of(aptitude));
        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(null);

        assertEquals("Section A", aptitude.getName());
        ArgumentCaptor<Section> createdSections =
                ArgumentCaptor.forClass(Section.class);
        verify(sectionRepository, org.mockito.Mockito.times(3))
                .save(createdSections.capture());
        assertEquals(
                List.of("Section B", "Section C"),
                createdSections.getAllValues().subList(1, 3).stream()
                        .map(Section::getName)
                        .toList()
        );
        assertEquals(aptitude, createdSections.getAllValues().get(0));
    }

    @Test
    void createsAllThreeGroupsForAnEmptyCatalog() {
        when(sectionRepository.findAll()).thenReturn(List.of());
        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(null);

        ArgumentCaptor<Section> createdSections =
                ArgumentCaptor.forClass(Section.class);
        verify(sectionRepository, org.mockito.Mockito.times(3))
                .save(createdSections.capture());
        assertEquals(
                List.of("Section A", "Section B", "Section C"),
                createdSections.getAllValues().stream()
                        .map(Section::getName)
                        .toList()
        );
    }

    @Test
    void leavesUnrecognizedExistingSectionsUntouched() {
        Section custom = Section.builder()
                .id(1L)
                .name("General Studies")
                .active(true)
                .build();
        when(sectionRepository.findAll()).thenReturn(List.of(custom));

        initializer.run(null);

        verify(sectionRepository, never()).save(any(Section.class));
    }
}
