package com.example.mocktest.dto.payment;

import com.example.mocktest.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentStatusResponse {

    private Long id;

    private String packageId;

    private String packageName;

    private Long amountInPaise;

    private String currency;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private PaymentStatus status;

    private LocalDateTime createdAt;
}
