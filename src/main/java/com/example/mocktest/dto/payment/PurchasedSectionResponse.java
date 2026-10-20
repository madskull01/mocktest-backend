package com.example.mocktest.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PurchasedSectionResponse {

    private Long sectionId;

    private String sectionName;

    private String packageName;

    private LocalDateTime purchasedAt;
}
