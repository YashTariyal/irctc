package com.irctc.user.dto;

import lombok.Data;

@Data
public class ReferralCodeResponse {
    private Long userId;
    private String referralCode;
    private Integer totalPoints;
    private Long totalReferrals;
}

