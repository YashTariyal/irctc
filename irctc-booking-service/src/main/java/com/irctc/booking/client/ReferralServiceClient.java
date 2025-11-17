package com.irctc.booking.client;

import com.irctc.booking.dto.ReferralRewardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "irctc-user-service", fallback = ReferralServiceClientFallback.class)
public interface ReferralServiceClient {

    @PostMapping("/api/users/referrals/reward")
    void awardReferralReward(@RequestBody ReferralRewardRequest request);
}

