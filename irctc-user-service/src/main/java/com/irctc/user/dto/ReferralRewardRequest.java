package com.irctc.user.dto;

import lombok.Data;

@Data
public class ReferralRewardRequest {
    private Long referredUserId;
    private Long referrerUserId;
    private Long bookingId;
    private Integer rewardPoints;
    private String description;
}

