package com.example.mocktest.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePaymentOrderRequest {

    @NotBlank
    @Pattern(regexp = "SECTION_A|SECTION_A_B|SECTION_A_B_C")
    private String packageId;
}
