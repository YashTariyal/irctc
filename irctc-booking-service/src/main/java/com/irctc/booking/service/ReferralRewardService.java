package com.irctc.booking.service;

import com.irctc.booking.client.ReferralServiceClient;
import com.irctc.booking.dto.ReferralRewardRequest;
import com.irctc.booking.entity.SimpleBooking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReferralRewardService {

    private static final Logger logger = LoggerFactory.getLogger(ReferralRewardService.class);

    private final ReferralServiceClient referralServiceClient;

    @Value("${referral.points.booking:100}")
    private int bookingRewardPoints;

    public ReferralRewardService(ReferralServiceClient referralServiceClient) {
        this.referralServiceClient = referralServiceClient;
    }

    public void recordReferralForBooking(SimpleBooking booking) {
        if (booking == null || booking.getUserId() == null) {
            return;
        }

        try {
            ReferralRewardRequest request = new ReferralRewardRequest();
            request.setReferredUserId(booking.getUserId());
            request.setBookingId(booking.getId());
            request.setRewardPoints(bookingRewardPoints);
            request.setDescription("Booking reward for PNR " + booking.getPnrNumber());
            referralServiceClient.awardReferralReward(request);
            logger.info("📣 Referral reward requested for booking {}", booking.getId());
        } catch (Exception ex) {
            logger.warn("Unable to notify referral service for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }
}

