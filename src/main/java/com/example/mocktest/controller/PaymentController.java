package com.example.mocktest.controller;

import com.example.mocktest.dto.payment.CreatePaymentOrderRequest;
import com.example.mocktest.dto.payment.AdminPaymentResponse;
import com.example.mocktest.dto.payment.PaymentOrderResponse;
import com.example.mocktest.dto.payment.PaymentStatusResponse;
import com.example.mocktest.dto.payment.PurchasedSectionResponse;
import com.example.mocktest.dto.payment.VerifyPaymentRequest;
import com.example.mocktest.security.AuthenticatedUser;
import com.example.mocktest.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePaymentOrderRequest request) {
        return ResponseEntity.ok(
                paymentService.createOrder(user.userId(), request.getPackageId()));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PaymentStatusResponse> verifyPayment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(
                paymentService.verifyCheckoutPayment(user.userId(), request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentHistory(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(
                paymentService.getPaymentHistory(user.userId()));
    }

    @GetMapping("/admin/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminPaymentResponse>> getAllPaymentHistory() {
                return ResponseEntity.ok(paymentService.getAllPaymentHistory());
    }

    @GetMapping("/purchased-sections")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PurchasedSectionResponse>> getPurchasedSections(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(
                paymentService.getPurchasedSections(user.userId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String rawBody) {
        paymentService.processWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
