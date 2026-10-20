package com.example.mocktest.service.payment;

import com.example.mocktest.exception.BadRequestException;
import com.example.mocktest.model.Payment;
import com.example.mocktest.model.Section;
import com.example.mocktest.model.User;
import com.example.mocktest.model.enums.PaymentStatus;
import com.example.mocktest.repository.PaymentRepository;
import com.example.mocktest.repository.SectionRepository;
import com.example.mocktest.repository.UserRepository;
import com.example.mocktest.repository.UserSectionEntitlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private static final String WEBHOOK_SECRET = "test-webhook-secret";

    private PaymentRepository paymentRepository;
    private UserSectionEntitlementRepository entitlementRepository;
    private SectionRepository sectionRepository;
    private PaymentService paymentService;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        entitlementRepository = mock(UserSectionEntitlementRepository.class);
        sectionRepository = mock(SectionRepository.class);
        Section section = Section.builder()
                .id(4L)
                .name("Section A")
                .active(true)
                .build();
        User user = User.builder().id(8L).name("Student").build();
        payment = Payment.builder()
                .id(12L)
                .user(user)
                .packageId("SECTION_A")
                .packageName("Section A")
                .amountInPaise(29_900L)
                .currency("INR")
                .razorpayOrderId("order_test123")
                .status(PaymentStatus.PENDING)
                .build();
        paymentService = new PaymentService(
                paymentRepository,
                entitlementRepository,
                mock(UserRepository.class),
                sectionRepository,
                mock(RazorpayGateway.class),
                WEBHOOK_SECRET,
                WEBHOOK_SECRET
        );
        when(paymentRepository.findByRazorpayOrderId("order_test123"))
                .thenReturn(Optional.of(payment));
        when(sectionRepository.findAll()).thenReturn(List.of(section));
        when(entitlementRepository.existsByUserIdAndSectionId(8L, 4L))
                .thenReturn(false);
    }

    @Test
    void capturedSignedWebhookGrantsEntitlementOnceWhenReplayed() throws Exception {
        String body = """
                {"event":"payment.captured","payload":{"payment":{"entity":{
                  "id":"pay_test123","order_id":"order_test123",
                  "amount":29900,"currency":"INR","status":"captured"
                }}}}
                """;
        String signature = sign(body, WEBHOOK_SECRET);

        paymentService.processWebhook(body, signature);
        paymentService.processWebhook(body, signature);

        verify(entitlementRepository, times(1)).save(any());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void invalidWebhookSignatureDoesNotReadOrChangePayment() {
        assertThrows(
                BadRequestException.class,
                () -> paymentService.processWebhook("{}", "not-a-valid-signature")
        );

        verify(paymentRepository, never()).findByRazorpayOrderId(any());
        verify(entitlementRepository, never()).save(any());
    }

    @Test
    void adminPaymentHistoryIncludesPurchaserAndPaymentDetails() {
        when(paymentRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(payment));

        var history = paymentService.getAllPaymentHistory();

        org.junit.jupiter.api.Assertions.assertEquals(1, history.size());
        org.junit.jupiter.api.Assertions.assertEquals(8L, history.get(0).getUserId());
        org.junit.jupiter.api.Assertions.assertEquals("Student", history.get(0).getUserName());
        org.junit.jupiter.api.Assertions.assertEquals("Section A", history.get(0).getPackageName());
        org.junit.jupiter.api.Assertions.assertEquals(29_900L, history.get(0).getAmountInPaise());
        org.junit.jupiter.api.Assertions.assertEquals(PaymentStatus.PENDING, history.get(0).getStatus());
    }

    private String sign(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(
                mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
