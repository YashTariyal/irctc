package com.irctc.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletPaymentRequest {
    @NotNull
    private Long bookingId;

    private Long userId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String currency = "INR";
    private String walletProvider;
    private String walletToken;
    private String deviceInfo;
    private String description;
}

