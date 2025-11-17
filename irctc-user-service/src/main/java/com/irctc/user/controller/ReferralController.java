package com.irctc.user.controller;

import com.irctc.user.dto.ReferralCodeResponse;
import com.irctc.user.dto.ReferralLeaderboardEntry;
import com.irctc.user.dto.ReferralRewardRequest;
import com.irctc.user.dto.ReferralSummaryResponse;
import com.irctc.user.service.ReferralService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @GetMapping("/{id}/referral-code")
    public ResponseEntity<ReferralCodeResponse> getReferralCode(@PathVariable Long id) {
        return ResponseEntity.ok(referralService.getReferralCode(id));
    }

    @GetMapping("/{id}/referrals")
    public ResponseEntity<ReferralSummaryResponse> getReferrals(@PathVariable Long id) {
        return ResponseEntity.ok(referralService.getReferralSummary(id));
    }

    @GetMapping("/referral-leaderboard")
    public ResponseEntity<List<ReferralLeaderboardEntry>> getLeaderboard(
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(referralService.getLeaderboard(limit));
    }

    @PostMapping("/referrals/reward")
    public ResponseEntity<Void> awardReferralReward(@RequestBody ReferralRewardRequest request) {
        referralService.recordBookingReward(request);
        return ResponseEntity.accepted().build();
    }
}

