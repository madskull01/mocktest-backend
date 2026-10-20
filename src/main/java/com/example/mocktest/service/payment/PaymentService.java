package com.example.mocktest.service.payment;

import com.example.mocktest.dto.payment.PaymentOrderResponse;
import com.example.mocktest.dto.payment.AdminPaymentResponse;
import com.example.mocktest.dto.payment.PaymentStatusResponse;
import com.example.mocktest.dto.payment.PurchasedSectionResponse;
import com.example.mocktest.dto.payment.VerifyPaymentRequest;
import com.example.mocktest.exception.BadRequestException;
import com.example.mocktest.exception.ConflictException;
import com.example.mocktest.exception.ForbiddenException;
import com.example.mocktest.exception.ResourceNotFoundException;
import com.example.mocktest.model.Payment;
import com.example.mocktest.model.Section;
import com.example.mocktest.model.User;
import com.example.mocktest.model.UserSectionEntitlement;
import com.example.mocktest.model.enums.PaymentStatus;
import com.example.mocktest.repository.PaymentRepository;
import com.example.mocktest.repository.SectionRepository;
import com.example.mocktest.repository.UserRepository;
import com.example.mocktest.repository.UserSectionEntitlementRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserSectionEntitlementRepository entitlementRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final RazorpayGateway razorpayGateway;
    private final ObjectMapper objectMapper;
    private final String keySecret;
    private final String webhookSecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            UserSectionEntitlementRepository entitlementRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            RazorpayGateway razorpayGateway,
            @Value("${app.razorpay.key-secret:}") String keySecret,
            @Value("${app.razorpay.webhook-secret:}") String webhookSecret) {
        this.paymentRepository = paymentRepository;
        this.entitlementRepository = entitlementRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.razorpayGateway = razorpayGateway;
        this.objectMapper = new ObjectMapper();
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    public PaymentOrderResponse createOrder(Long userId, String packageId) {
        PaidPackage paidPackage;
        try {
            paidPackage = PaidPackage.fromId(packageId);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported package");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (razorpayGateway.getKeyId() == null
                || razorpayGateway.getKeyId().isBlank()) {
            throw new BadRequestException("Razorpay key ID is not configured");
        }
        List<Section> sections = resolveSections(paidPackage);
        List<Long> sectionIds = sections.stream().map(Section::getId).toList();
        if (sectionIds.stream().allMatch(id ->
                entitlementRepository.existsByUserIdAndSectionId(userId, id))) {
            throw new ConflictException("You already own all sections in this package");
        }

        String receipt = "mt_" + UUID.randomUUID().toString().replace("-", "");
        JsonNode order = razorpayGateway.createOrder(
                paidPackage.amountInPaise(), receipt);
        String orderId = requiredText(order, "id");
        if (order.path("amount").asLong(-1) != paidPackage.amountInPaise()
                || !"INR".equals(order.path("currency").asText())) {
            throw new IllegalStateException("Razorpay returned an unexpected order amount");
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .user(user)
                .packageId(paidPackage.id())
                .packageName(paidPackage.displayName())
                .amountInPaise(paidPackage.amountInPaise())
                .currency("INR")
                .razorpayOrderId(orderId)
                .status(PaymentStatus.PENDING)
                .signatureVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .orderId(orderId)
                .amount(paidPackage.amountInPaise())
                .currency("INR")
                .keyId(razorpayGateway.getKeyId())
                .packageId(paidPackage.id())
                .packageName(paidPackage.displayName())
                .build();
    }

    public PaymentStatusResponse verifyCheckoutPayment(
            Long userId,
            VerifyPaymentRequest request) {
        Payment payment = paymentRepository
                .findByRazorpayOrderIdAndUserId(
                        request.getRazorpayOrderId(), userId)
                .orElseThrow(() -> new ForbiddenException(
                        "Payment order does not belong to this user"));
        if (payment.getStatus() == PaymentStatus.PAID) {
            return mapPayment(payment);
        }

        String expectedSignature = hmacHex(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                requireKeySecret());
        if (!constantTimeEquals(expectedSignature, request.getRazorpaySignature())) {
            throw new BadRequestException("Razorpay payment signature is invalid");
        }

        JsonNode remotePayment = razorpayGateway.fetchPayment(
                request.getRazorpayPaymentId());
        validatePaymentDetails(payment, remotePayment);
        if (!"captured".equals(remotePayment.path("status").asText())) {
            return mapPayment(payment);
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setSignatureVerified(true);
        markPaidAndGrantEntitlements(payment);
        return mapPayment(payment);
    }

    public void processWebhook(String rawBody, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new BadRequestException("Razorpay webhook secret is not configured");
        }
        String expectedSignature = hmacHex(rawBody, webhookSecret);
        if (!constantTimeEquals(expectedSignature, signature)) {
            throw new BadRequestException("Razorpay webhook signature is invalid");
        }

        JsonNode event;
        try {
            event = objectMapper.readTree(rawBody);
        } catch (Exception exception) {
            throw new BadRequestException("Razorpay webhook payload is invalid");
        }

        String eventName = event.path("event").asText();
        JsonNode paymentEntity = event.path("payload")
                .path("payment").path("entity");
        if (!"payment.captured".equals(eventName)
                && !"payment.failed".equals(eventName)) {
            return;
        }
        String orderId = requiredText(paymentEntity, "order_id");
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment order was not found"));
        validatePaymentDetails(payment, paymentEntity);
        if ("payment.captured".equals(eventName)) {
            payment.setRazorpayPaymentId(requiredText(paymentEntity, "id"));
            payment.setSignatureVerified(true);
            markPaidAndGrantEntitlements(payment);
        } else if (payment.getStatus() != PaymentStatus.PAID) {
            payment.setRazorpayPaymentId(
                    paymentEntity.path("id").asText(null));
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentStatusResponse> getPaymentHistory(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapPayment)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> getAllPaymentHistory() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(payment -> AdminPaymentResponse.builder()
                        .id(payment.getId())
                        .userId(payment.getUser().getId())
                        .userName(payment.getUser().getName())
                        .userEmail(payment.getUser().getEmail())
                        .packageName(payment.getPackageName())
                        .amountInPaise(payment.getAmountInPaise())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .createdAt(payment.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchasedSectionResponse> getPurchasedSections(Long userId) {
        return entitlementRepository.findByUserIdOrderByGrantedAtDesc(userId)
                .stream()
                .map(entitlement -> PurchasedSectionResponse.builder()
                        .sectionId(entitlement.getSection().getId())
                        .sectionName(entitlement.getSection().getName())
                        .packageName(entitlement.getPayment().getPackageName())
                        .purchasedAt(entitlement.getGrantedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasSectionAccess(Long userId, Section section) {
        PaidSection paidSection = PaidSection.fromName(section.getName());
        return paidSection == null
                || entitlementRepository.existsByUserIdAndSectionId(
                        userId, section.getId());
    }

    private List<Section> resolveSections(PaidPackage paidPackage) {
        List<Section> allSections = sectionRepository.findAll();
        List<Section> resolved = new ArrayList<>();
        for (PaidSection code : paidPackage.sections()) {
            Section section = allSections.stream()
                    .filter(candidate ->
                            code == PaidSection.fromName(candidate.getName()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Paid package is unavailable: configure Section "
                                    + code.name() + " first"));
            resolved.add(section);
        }
        return resolved;
    }

    private void markPaidAndGrantEntitlements(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }
        payment.setStatus(PaymentStatus.PAID);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaidPackage paidPackage = PaidPackage.fromId(payment.getPackageId());
        for (Section section : resolveSections(paidPackage)) {
            if (!entitlementRepository.existsByUserIdAndSectionId(
                    payment.getUser().getId(), section.getId())) {
                entitlementRepository.save(UserSectionEntitlement.builder()
                        .user(payment.getUser())
                        .section(section)
                        .payment(payment)
                        .grantedAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    private void validatePaymentDetails(Payment payment, JsonNode remotePayment) {
        if (!payment.getRazorpayOrderId().equals(
                remotePayment.path("order_id").asText())
                || payment.getAmountInPaise() != remotePayment.path("amount").asLong(-1)
                || !payment.getCurrency().equals(
                        remotePayment.path("currency").asText())) {
            throw new BadRequestException(
                    "Payment details do not match the expected order");
        }
    }

    private PaymentStatusResponse mapPayment(Payment payment) {
        return PaymentStatusResponse.builder()
                .id(payment.getId())
                .packageId(payment.getPackageId())
                .packageName(payment.getPackageName())
                .amountInPaise(payment.getAmountInPaise())
                .currency(payment.getCurrency())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private String requireKeySecret() {
        if (keySecret == null || keySecret.isBlank()) {
            throw new BadRequestException("Razorpay key secret is not configured");
        }
        return keySecret;
    }

    private String hmacHex(String value, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(
                    mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not verify Razorpay signature", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                actual.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText();
        if (value.isBlank() || "null".equals(value)) {
            throw new IllegalStateException(
                    "Razorpay response is missing " + field);
        }
        return value;
    }
}
