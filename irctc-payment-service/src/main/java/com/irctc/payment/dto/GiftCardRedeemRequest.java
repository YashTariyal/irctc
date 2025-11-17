package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GiftCardRedeemRequest {
    private String code;
    private BigDecimal amount;
    private Long bookingId;
}

