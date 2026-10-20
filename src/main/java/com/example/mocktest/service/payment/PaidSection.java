package com.example.mocktest.service.payment;

import java.util.Locale;

public enum PaidSection {
    A,
    B,
    C;

    public static PaidSection fromName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s_-]+", "");
        if (normalized.startsWith("SECTION")) {
            normalized = normalized.substring("SECTION".length());
        }
        return switch (normalized) {
            case "A" -> A;
            case "B" -> B;
            case "C" -> C;
            default -> null;
        };
    }
}
