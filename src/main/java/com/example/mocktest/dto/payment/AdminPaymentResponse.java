package com.example.mocktest.dto.payment;

import com.example.mocktest.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPaymentResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String userEmail;

    private String packageName;

    private Long amountInPaise;

    private String currency;

    private PaymentStatus status;

    private LocalDateTime createdAt;
}
