package com.example.mocktest.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOrderResponse {

    private String orderId;

    private Long amount;

    private String currency;

    private String keyId;

    private String packageId;

    private String packageName;
}
