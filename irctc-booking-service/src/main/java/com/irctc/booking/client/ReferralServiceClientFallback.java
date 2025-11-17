package com.irctc.booking.client;

import com.irctc.booking.dto.ReferralRewardRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReferralServiceClientFallback implements ReferralServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ReferralServiceClientFallback.class);

    @Override
    public void awardReferralReward(ReferralRewardRequest request) {
        logger.warn("Referral service unavailable, unable to award reward for booking {}", request.getBookingId());
    }
}

