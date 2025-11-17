package com.irctc.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class ReferralSummaryResponse {
    private Long userId;
    private Integer totalPoints;
    private Integer totalReferrals;
    private List<Entry> referrals = Collections.emptyList();

    @Data
    public static class Entry {
        private Long referralId;
        private Long referredUserId;
        private Long bookingId;
        private String status;
        private Integer rewardPoints;
        private LocalDateTime createdAt;
    }
}

