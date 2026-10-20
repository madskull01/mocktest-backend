package com.example.mocktest.repository;

import com.example.mocktest.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderIdAndUserId(
            String razorpayOrderId,
            Long userId
    );

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
