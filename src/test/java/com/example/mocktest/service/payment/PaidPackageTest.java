package com.example.mocktest.service.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaidPackageTest {

    @Test
    void packagesUseExpectedPaiseValuesAndSectionMembership() {
        assertEquals(29_900L, PaidPackage.fromId("SECTION_A").amountInPaise());
        assertEquals(49_900L, PaidPackage.fromId("SECTION_A_B").amountInPaise());
        assertEquals(79_900L, PaidPackage.fromId("SECTION_A_B_C").amountInPaise());
        assertEquals(3, PaidPackage.fromId("SECTION_A_B_C").sections().size());
    }

    @Test
    void sectionResolverAcceptsCommonSectionNameFormats() {
        assertEquals(PaidSection.A, PaidSection.fromName("Section A"));
        assertEquals(PaidSection.B, PaidSection.fromName("section_b"));
        assertEquals(PaidSection.C, PaidSection.fromName("C"));
        assertNull(PaidSection.fromName("General Studies"));
    }
}
