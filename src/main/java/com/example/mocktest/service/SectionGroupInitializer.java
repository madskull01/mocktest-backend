package com.example.mocktest.service;

import com.example.mocktest.model.Section;
import com.example.mocktest.repository.SectionRepository;
import com.example.mocktest.service.payment.PaidSection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SectionGroupInitializer implements ApplicationRunner {

    private final SectionRepository sectionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Section> sections = sectionRepository.findAll();
        Set<PaidSection> configuredGroups = EnumSet.noneOf(PaidSection.class);
        for (Section section : sections) {
            PaidSection group = PaidSection.fromName(section.getName());
            if (group != null) {
                configuredGroups.add(group);
            }
        }

        if (configuredGroups.containsAll(EnumSet.allOf(PaidSection.class))) {
            return;
        }

        if (sections.isEmpty()) {
            createMissingGroups(configuredGroups);
            return;
        }

        boolean sectionsAreNamedForGroups = sections.stream()
                .allMatch(section ->
                        PaidSection.fromName(section.getName()) != null);
        if (sectionsAreNamedForGroups) {
            createMissingGroups(configuredGroups);
            return;
        }

        if (configuredGroups.isEmpty()
                && sections.size() == 1
                && "aptitude".equals(
                        sections.get(0).getName().trim().toLowerCase(Locale.ROOT))) {
            Section existingSection = sections.get(0);
            existingSection.setName("Section A");
            sectionRepository.save(existingSection);
            configuredGroups.add(PaidSection.A);
            createMissingGroups(configuredGroups);
            log.info("Migrated the existing Aptitude section to Section A");
            return;
        }

        log.warn(
                "Skipped automatic A/B/C section setup because existing sections "
                        + "do not match the known initial catalog");
    }

    private void createMissingGroups(Set<PaidSection> configuredGroups) {
        for (PaidSection group : PaidSection.values()) {
            if (configuredGroups.contains(group)) {
                continue;
            }
            Section section = Section.builder()
                    .name("Section " + group.name())
                    .description("Subjects and practice tests in Section "
                            + group.name())
                    .active(true)
                    .build();
            sectionRepository.save(section);
            configuredGroups.add(group);
        }
    }
}
