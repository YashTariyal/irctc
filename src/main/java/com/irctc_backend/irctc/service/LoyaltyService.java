package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.LoyaltyAccountResponse;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for loyalty points and rewards management
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@Transactional
public class LoyaltyService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyService.class);
    
    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;
    
    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;
    
    @Autowired
    private RewardRepository rewardRepository;
    
    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create loyalty account for user
     */
    public LoyaltyAccount createLoyaltyAccount(User user) {
        logger.info("Creating loyalty account for user: {}", user.getUsername());
        
        // Check if user already has a loyalty account
        Optional<LoyaltyAccount> existingAccount = loyaltyAccountRepository.findByUser(user);
        if (existingAccount.isPresent()) {
            logger.warn("User {} already has a loyalty account", user.getUsername());
            return existingAccount.get();
        }
        
        // Generate unique loyalty number
        String loyaltyNumber = generateLoyaltyNumber();
        
        // Create new loyalty account
        LoyaltyAccount loyaltyAccount = new LoyaltyAccount(user, loyaltyNumber);
        loyaltyAccount = loyaltyAccountRepository.save(loyaltyAccount);
        
        // Create welcome bonus transaction
        createWelcomeBonusTransaction(loyaltyAccount);
        
        logger.info("Created loyalty account {} for user {}", loyaltyNumber, user.getUsername());
        return loyaltyAccount;
    }
    
    /**
     * Get loyalty account by user
     */
    public Optional<LoyaltyAccountResponse> getLoyaltyAccount(User user) {
        logger.info("Getting loyalty account for user: {}", user.getUsername());
        
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByUser(user);
        if (account.isPresent()) {
            return Optional.of(new LoyaltyAccountResponse(account.get()));
        }
        
        return Optional.empty();
    }
    
    /**
     * Earn points for booking
     */
    public void earnPointsForBooking(Booking booking) {
        logger.info("Processing points for booking: {}", booking.getPnrNumber());
        
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByUser(booking.getUser());
        if (account.isEmpty()) {
            logger.warn("No loyalty account found for user: {}", booking.getUser().getUsername());
            return;
        }
        
        LoyaltyAccount loyaltyAccount = account.get();
        
        // Calculate points based on booking amount and tier
        BigDecimal pointsEarned = calculatePointsForBooking(booking, loyaltyAccount);
        
        // Update loyalty account
        loyaltyAccount.setTotalPoints(loyaltyAccount.getTotalPoints().add(pointsEarned));
        loyaltyAccount.setAvailablePoints(loyaltyAccount.getAvailablePoints().add(pointsEarned));
        loyaltyAccount.setTotalSpent(loyaltyAccount.getTotalSpent().add(booking.getTotalFare()));
        loyaltyAccount.setTotalBookings(loyaltyAccount.getTotalBookings() + 1);
        loyaltyAccount.setLastActivityDate(LocalDateTime.now());
        
        // Check for tier upgrade
        checkAndUpgradeTier(loyaltyAccount);
        
        loyaltyAccountRepository.save(loyaltyAccount);
        
        // Create transaction record
        createEarnedPointsTransaction(loyaltyAccount, pointsEarned, booking);
        
        logger.info("Awarded {} points to user {} for booking {}", 
                   pointsEarned, booking.getUser().getUsername(), booking.getPnrNumber());
    }
    
    /**
     * Redeem reward
     */
    public RewardRedemption redeemReward(User user, Long rewardId, String notes) {
        logger.info("Processing reward redemption for user: {}, reward: {}", user.getUsername(), rewardId);
        
        // Get loyalty account
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByUser(user);
        if (account.isEmpty()) {
            throw new RuntimeException("No loyalty account found for user");
        }
        
        LoyaltyAccount loyaltyAccount = account.get();
        
        // Get reward
        Optional<Reward> reward = rewardRepository.findById(rewardId);
        if (reward.isEmpty()) {
            throw new RuntimeException("Reward not found");
        }
        
        Reward rewardEntity = reward.get();
        
        // Validate redemption
        validateRewardRedemption(loyaltyAccount, rewardEntity);
        
        // Create redemption
        String redemptionCode = generateRedemptionCode();
        RewardRedemption redemption = new RewardRedemption(loyaltyAccount, rewardEntity, redemptionCode, rewardEntity.getPointsRequired());
        redemption.setNotes(notes);
        redemption.setStatus(RewardRedemption.RedemptionStatus.ACTIVE);
        
        redemption = rewardRedemptionRepository.save(redemption);
        
        // Update loyalty account
        loyaltyAccount.setAvailablePoints(loyaltyAccount.getAvailablePoints().subtract(rewardEntity.getPointsRequired()));
        loyaltyAccount.setRedeemedPoints(loyaltyAccount.getRedeemedPoints().add(rewardEntity.getPointsRequired()));
        loyaltyAccount.setLastActivityDate(LocalDateTime.now());
        loyaltyAccountRepository.save(loyaltyAccount);
        
        // Create transaction record
        createRedeemedPointsTransaction(loyaltyAccount, rewardEntity.getPointsRequired(), redemption);
        
        // Update reward redemption count
        rewardEntity.setRedemptionCount(rewardEntity.getRedemptionCount() + 1);
        rewardRepository.save(rewardEntity);
        
        logger.info("Successfully redeemed reward {} for user {}", rewardEntity.getName(), user.getUsername());
        return redemption;
    }
    
    /**
     * Get available rewards for user
     */
    public List<Reward> getAvailableRewards(User user) {
        logger.info("Getting available rewards for user: {}", user.getUsername());
        
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByUser(user);
        if (account.isEmpty()) {
            return List.of();
        }
        
        LoyaltyAccount loyaltyAccount = account.get();
        String tier = loyaltyAccount.getTier().name();
        
        return rewardRepository.findRewardsAvailableForTier(tier);
    }
    
    /**
     * Get user's redemption history
     */
    public List<RewardRedemption> getRedemptionHistory(User user) {
        logger.info("Getting redemption history for user: {}", user.getUsername());
        
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByUser(user);
        if (account.isEmpty()) {
            return List.of();
        }
        
        return rewardRedemptionRepository.findByLoyaltyAccount(account.get());
    }
    
    /**
     * Process expired points
     */
    public void processExpiredPoints() {
        logger.info("Processing expired points");
        
        LocalDateTime currentDate = LocalDateTime.now();
        List<LoyaltyTransaction> expiredTransactions = loyaltyTransactionRepository.findExpiredTransactions(currentDate);
        
        for (LoyaltyTransaction transaction : expiredTransactions) {
            if (transaction.getPoints().compareTo(BigDecimal.ZERO) > 0) {
                // Mark transaction as expired
                transaction.setIsExpired(true);
                loyaltyTransactionRepository.save(transaction);
                
                // Update loyalty account
                LoyaltyAccount account = transaction.getLoyaltyAccount();
                account.setAvailablePoints(account.getAvailablePoints().subtract(transaction.getPoints()));
                account.setExpiredPoints(account.getExpiredPoints().add(transaction.getPoints()));
                loyaltyAccountRepository.save(account);
                
                // Create expired transaction record
                createExpiredPointsTransaction(account, transaction.getPoints());
                
                logger.info("Expired {} points for user {}", transaction.getPoints(), account.getUser().getUsername());
            }
        }
    }
    
    // Helper methods
    
    private String generateLoyaltyNumber() {
        return "IRCTC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    private String generateRedemptionCode() {
        return "RED" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    private BigDecimal calculatePointsForBooking(Booking booking, LoyaltyAccount loyaltyAccount) {
        // Base points: 1 point per ₹10 spent
        BigDecimal basePoints = booking.getTotalFare().divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
        
        // Apply tier multiplier
        BigDecimal multiplier = new BigDecimal(loyaltyAccount.getTier().getMultiplier());
        BigDecimal finalPoints = basePoints.multiply(multiplier);
        
        return finalPoints;
    }
    
    private void checkAndUpgradeTier(LoyaltyAccount loyaltyAccount) {
        LoyaltyAccount.LoyaltyTier currentTier = loyaltyAccount.getTier();
        LoyaltyAccount.LoyaltyTier[] tiers = LoyaltyAccount.LoyaltyTier.values();
        
        for (LoyaltyAccount.LoyaltyTier tier : tiers) {
            if (loyaltyAccount.getTotalPoints().compareTo(new BigDecimal(tier.getMinimumPoints())) >= 0) {
                if (tier.ordinal() > currentTier.ordinal()) {
                    loyaltyAccount.setTier(tier);
                    logger.info("Upgraded user {} to {} tier", loyaltyAccount.getUser().getUsername(), tier.getDisplayName());
                }
            }
        }
    }
    
    private void validateRewardRedemption(LoyaltyAccount loyaltyAccount, Reward reward) {
        // Check if reward is active
        if (!reward.getIsActive()) {
            throw new RuntimeException("Reward is not active");
        }
        
        // Check if user has enough points
        if (loyaltyAccount.getAvailablePoints().compareTo(reward.getPointsRequired()) < 0) {
            throw new RuntimeException("Insufficient points for redemption");
        }
        
        // Check tier requirement
        if (!isTierEligible(loyaltyAccount.getTier().name(), reward.getMinTierRequired())) {
            throw new RuntimeException("Tier requirement not met");
        }
        
        // Check redemption limit
        if (reward.getRedemptionLimit() != null && 
            reward.getRedemptionCount() >= reward.getRedemptionLimit()) {
            throw new RuntimeException("Reward redemption limit reached");
        }
    }
    
    private boolean isTierEligible(String userTier, String requiredTier) {
        String[] tierOrder = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"};
        
        int userTierIndex = -1;
        int requiredTierIndex = -1;
        
        for (int i = 0; i < tierOrder.length; i++) {
            if (tierOrder[i].equals(userTier)) {
                userTierIndex = i;
            }
            if (tierOrder[i].equals(requiredTier)) {
                requiredTierIndex = i;
            }
        }
        
        return userTierIndex >= requiredTierIndex;
    }
    
    private void createWelcomeBonusTransaction(LoyaltyAccount loyaltyAccount) {
        BigDecimal welcomePoints = new BigDecimal("100"); // Welcome bonus
        
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            loyaltyAccount, 
            LoyaltyTransaction.TransactionType.BONUS, 
            welcomePoints, 
            "Welcome bonus points"
        );
        transaction.setReferenceType(LoyaltyTransaction.ReferenceType.ADMIN);
        transaction.setMultiplierApplied(BigDecimal.ONE);
        
        loyaltyTransactionRepository.save(transaction);
        
        // Update account
        loyaltyAccount.setTotalPoints(loyaltyAccount.getTotalPoints().add(welcomePoints));
        loyaltyAccount.setAvailablePoints(loyaltyAccount.getAvailablePoints().add(welcomePoints));
        loyaltyAccountRepository.save(loyaltyAccount);
    }
    
    private void createEarnedPointsTransaction(LoyaltyAccount loyaltyAccount, BigDecimal points, Booking booking) {
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            loyaltyAccount, 
            LoyaltyTransaction.TransactionType.EARNED, 
            points, 
            "Points earned from booking " + booking.getPnrNumber()
        );
        transaction.setReferenceId(booking.getId().toString());
        transaction.setReferenceType(LoyaltyTransaction.ReferenceType.BOOKING);
        transaction.setMultiplierApplied(new BigDecimal(loyaltyAccount.getTier().getMultiplier()));
        
        loyaltyTransactionRepository.save(transaction);
    }
    
    private void createRedeemedPointsTransaction(LoyaltyAccount loyaltyAccount, BigDecimal points, RewardRedemption redemption) {
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            loyaltyAccount, 
            LoyaltyTransaction.TransactionType.REDEEMED, 
            points, 
            "Points redeemed for " + redemption.getReward().getName()
        );
        transaction.setReferenceId(redemption.getId().toString());
        transaction.setReferenceType(LoyaltyTransaction.ReferenceType.PROMOTION);
        
        loyaltyTransactionRepository.save(transaction);
    }
    
    private void createExpiredPointsTransaction(LoyaltyAccount loyaltyAccount, BigDecimal points) {
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            loyaltyAccount, 
            LoyaltyTransaction.TransactionType.EXPIRED, 
            points, 
            "Points expired due to inactivity"
        );
        transaction.setReferenceType(LoyaltyTransaction.ReferenceType.ADMIN);
        
        loyaltyTransactionRepository.save(transaction);
    }
}
