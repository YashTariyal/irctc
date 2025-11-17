package com.irctc.user.dto;

import lombok.Data;

@Data
public class ReferralLeaderboardEntry {
    private Long userId;
    private String username;
    private String fullName;
    private Integer referralPoints;
    private Long totalReferrals;
    private Integer rank;
}

