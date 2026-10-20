package com.example.mocktest.service.payment;

import java.util.List;

public enum PaidPackage {
    SECTION_A("SECTION_A", "Section A", 29_900L, List.of(PaidSection.A)),
    SECTION_A_B("SECTION_A_B", "Section A + Section B", 49_900L,
            List.of(PaidSection.A, PaidSection.B)),
    SECTION_A_B_C("SECTION_A_B_C", "Sections A + B + C", 79_900L,
            List.of(PaidSection.A, PaidSection.B, PaidSection.C));

    private final String id;
    private final String displayName;
    private final long amountInPaise;
    private final List<PaidSection> sections;

    PaidPackage(String id, String displayName, long amountInPaise,
                List<PaidSection> sections) {
        this.id = id;
        this.displayName = displayName;
        this.amountInPaise = amountInPaise;
        this.sections = sections;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public long amountInPaise() {
        return amountInPaise;
    }

    public List<PaidSection> sections() {
        return sections;
    }

    public static PaidPackage fromId(String id) {
        for (PaidPackage paidPackage : values()) {
            if (paidPackage.id.equals(id)) {
                return paidPackage;
            }
        }
        throw new IllegalArgumentException("Unsupported package");
    }
}
