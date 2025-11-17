package com.irctc.user.service;

import com.irctc.user.dto.ReferralCodeResponse;
import com.irctc.user.dto.ReferralLeaderboardEntry;
import com.irctc.user.dto.ReferralRewardRequest;
import com.irctc.user.dto.ReferralSummaryResponse;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.UserReferral;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.UserReferralRepository;
import com.irctc.user.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReferralService {

    private static final Logger logger = LoggerFactory.getLogger(ReferralService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SimpleUserRepository userRepository;
    private final UserReferralRepository referralRepository;

    @Value("${referral.points.signup:50}")
    private int signupRewardPoints;

    @Value("${referral.points.booking:100}")
    private int bookingRewardPoints;

    public ReferralService(SimpleUserRepository userRepository,
                           UserReferralRepository referralRepository) {
        this.userRepository = userRepository;
        this.referralRepository = referralRepository;
    }

    @Transactional
    public void handlePostRegistration(SimpleUser newUser, String referralCode) {
        ensureReferralCode(newUser);

        if (referralCode == null || referralCode.isBlank()) {
            return;
        }

        Optional<SimpleUser> referrerOpt = userRepository.findByReferralCode(referralCode.trim());
        if (referrerOpt.isEmpty()) {
            logger.warn("Referral code {} not found, skipping referral linkage", referralCode);
            return;
        }

        SimpleUser referrer = referrerOpt.get();
        if (referrer.getId().equals(newUser.getId())) {
            logger.warn("User {} attempted to refer themselves, ignoring", newUser.getId());
            return;
        }

        newUser.setReferredByUserId(referrer.getId());
        userRepository.save(newUser);

        UserReferral referral = new UserReferral();
        referral.setReferrerUserId(referrer.getId());
        referral.setReferredUserId(newUser.getId());
        referral.setReferralCodeUsed(referralCode);
        referral.setStatus("REGISTERED");
        referral.setRewardPoints(signupRewardPoints);
        referral.setTenantId(TenantContext.getTenantId());
        referralRepository.save(referral);

        incrementReferralPoints(referrer, signupRewardPoints);
        logger.info("✅ Referral registered: referrer={}, referred={}, points={}",
            referrer.getId(), newUser.getId(), signupRewardPoints);
    }

    public ReferralCodeResponse getReferralCode(Long userId) {
        SimpleUser user = userRepository.findById(userId)
            .orElseThrow(() -> new com.irctc.user.exception.EntityNotFoundException("User", userId));
        ensureReferralCode(user);

        ReferralCodeResponse response = new ReferralCodeResponse();
        response.setUserId(userId);
        response.setReferralCode(user.getReferralCode());
        response.setTotalPoints(Optional.ofNullable(user.getReferralPoints()).orElse(0));
        response.setTotalReferrals(referralRepository.countByReferrerUserId(userId));
        return response;
    }

    public ReferralSummaryResponse getReferralSummary(Long userId) {
        SimpleUser user = userRepository.findById(userId)
            .orElseThrow(() -> new com.irctc.user.exception.EntityNotFoundException("User", userId));
        List<UserReferral> referrals = referralRepository.findByReferrerUserIdOrderByCreatedAtDesc(userId);

        ReferralSummaryResponse response = new ReferralSummaryResponse();
        response.setUserId(userId);
        response.setTotalPoints(Optional.ofNullable(user.getReferralPoints()).orElse(0));
        response.setTotalReferrals(referrals.size());
        response.setReferrals(referrals.stream().map(r -> {
            ReferralSummaryResponse.Entry entry = new ReferralSummaryResponse.Entry();
            entry.setReferralId(r.getId());
            entry.setReferredUserId(r.getReferredUserId());
            entry.setBookingId(r.getBookingId());
            entry.setStatus(r.getStatus());
            entry.setRewardPoints(Optional.ofNullable(r.getRewardPoints()).orElse(0));
            entry.setCreatedAt(r.getCreatedAt());
            return entry;
        }).collect(Collectors.toList()));
        return response;
    }

    public List<ReferralLeaderboardEntry> getLeaderboard(int limit) {
        List<SimpleUser> topUsers = userRepository.findTop10ByOrderByReferralPointsDesc()
            .stream()
            .filter(user -> Optional.ofNullable(user.getReferralPoints()).orElse(0) > 0)
            .sorted(Comparator.comparing(u -> Optional.ofNullable(u.getReferralPoints()).orElse(0), Comparator.reverseOrder()))
            .limit(limit)
            .collect(Collectors.toList());

        int[] rank = {1};
        return topUsers.stream()
            .map(user -> {
                ReferralLeaderboardEntry entry = new ReferralLeaderboardEntry();
                entry.setUserId(user.getId());
                entry.setUsername(user.getUsername());
                entry.setFullName(String.format("%s %s",
                    Optional.ofNullable(user.getFirstName()).orElse(""),
                    Optional.ofNullable(user.getLastName()).orElse("")).trim());
                entry.setReferralPoints(Optional.ofNullable(user.getReferralPoints()).orElse(0));
                entry.setTotalReferrals(referralRepository.countByReferrerUserId(user.getId()));
                entry.setRank(rank[0]++);
                return entry;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void recordBookingReward(ReferralRewardRequest request) {
        if (request.getReferredUserId() == null && request.getReferrerUserId() == null) {
            logger.warn("Referral reward request missing identifiers {}", request);
            return;
        }

        Optional<UserReferral> referralOpt = Optional.empty();
        if (request.getReferredUserId() != null) {
            referralOpt = referralRepository.findByReferredUserId(request.getReferredUserId());
        }

        referralOpt.ifPresentOrElse(referral -> {
            referral.setBookingId(request.getBookingId());
            referral.setStatus("BOOKED");
            int reward = request.getRewardPoints() != null ? request.getRewardPoints() : bookingRewardPoints;
            referral.setRewardPoints(Optional.ofNullable(referral.getRewardPoints()).orElse(0) + reward);
            referralRepository.save(referral);

            userRepository.findById(referral.getReferrerUserId())
                .ifPresent(referrer -> incrementReferralPoints(referrer, reward));
        }, () -> {
            if (request.getReferrerUserId() != null) {
                SimpleUser referrer = userRepository.findById(request.getReferrerUserId())
                    .orElseThrow(() -> new com.irctc.user.exception.EntityNotFoundException("User", request.getReferrerUserId()));
                UserReferral referral = new UserReferral();
                referral.setReferrerUserId(referrer.getId());
                referral.setReferredUserId(request.getReferredUserId());
                referral.setBookingId(request.getBookingId());
                referral.setStatus("BOOKED");
                int reward = request.getRewardPoints() != null ? request.getRewardPoints() : bookingRewardPoints;
                referral.setRewardPoints(reward);
                referral.setTenantId(TenantContext.getTenantId());
                referralRepository.save(referral);
                incrementReferralPoints(referrer, reward);
            }
        });
    }

    private void incrementReferralPoints(SimpleUser user, int points) {
        user.setReferralPoints(Optional.ofNullable(user.getReferralPoints()).orElse(0) + points);
        userRepository.save(user);
    }

    private void ensureReferralCode(SimpleUser user) {
        if (user.getReferralCode() != null && !user.getReferralCode().isBlank()) {
            return;
        }
        user.setReferralCode(generateReferralCode(user.getId()));
        userRepository.save(user);
    }

    private String generateReferralCode(Long userId) {
        String base = Long.toString(userId != null ? userId : RANDOM.nextLong(1_000_000L), 36).toUpperCase(Locale.ROOT);
        String random = Integer.toString(RANDOM.nextInt(1_000_000), 36).toUpperCase(Locale.ROOT);
        String candidate = ("RF" + base + random).substring(0, Math.min(10, ("RF" + base + random).length()));

        while (userRepository.findByReferralCode(candidate).isPresent()) {
            candidate = candidate + RANDOM.nextInt(9);
        }
        return candidate;
    }
}

