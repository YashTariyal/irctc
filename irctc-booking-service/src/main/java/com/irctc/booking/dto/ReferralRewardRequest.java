package com.irctc.booking.dto;

import lombok.Data;

@Data
public class ReferralRewardRequest {
    private Long referredUserId;
    private Long bookingId;
    private Integer rewardPoints;
    private String description;
}

