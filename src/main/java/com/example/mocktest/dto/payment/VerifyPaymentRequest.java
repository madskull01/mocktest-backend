package com.example.mocktest.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyPaymentRequest {

    @NotBlank
    @Pattern(regexp = "order_[A-Za-z0-9]+")
    private String razorpayOrderId;

    @NotBlank
    @Pattern(regexp = "pay_[A-Za-z0-9]+")
    private String razorpayPaymentId;

    @NotBlank
    @Pattern(regexp = "[A-Fa-f0-9]{64}")
    private String razorpaySignature;
}
