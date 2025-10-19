package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.LoyaltyAccount;
import com.irctc_backend.irctc.entity.Reward;
import com.irctc_backend.irctc.entity.RewardRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RewardRedemption entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    
    /**
     * Find redemptions by loyalty account
     */
    List<RewardRedemption> findByLoyaltyAccount(LoyaltyAccount loyaltyAccount);
    
    /**
     * Find redemptions by loyalty account with pagination
     */
    Page<RewardRedemption> findByLoyaltyAccount(LoyaltyAccount loyaltyAccount, Pageable pageable);
    
    /**
     * Find redemption by redemption code
     */
    Optional<RewardRedemption> findByRedemptionCode(String redemptionCode);
    
    /**
     * Find redemptions by status
     */
    List<RewardRedemption> findByStatus(RewardRedemption.RedemptionStatus status);
    
    /**
     * Find redemptions by loyalty account and status
     */
    List<RewardRedemption> findByLoyaltyAccountAndStatus(LoyaltyAccount loyaltyAccount, 
                                                        RewardRedemption.RedemptionStatus status);
    
    /**
     * Find redemptions by reward
     */
    List<RewardRedemption> findByReward(Reward reward);
    
    /**
     * Find expired redemptions
     */
    @Query("SELECT rr FROM RewardRedemption rr WHERE rr.expiryDate <= :currentDate AND rr.status = 'ACTIVE'")
    List<RewardRedemption> findExpiredRedemptions(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find redemptions created in date range
     */
    @Query("SELECT rr FROM RewardRedemption rr WHERE rr.redemptionDate BETWEEN :startDate AND :endDate")
    List<RewardRedemption> findRedemptionsBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find active redemptions by loyalty account
     */
    @Query("SELECT rr FROM RewardRedemption rr WHERE rr.loyaltyAccount = :loyaltyAccount AND rr.status = 'ACTIVE' AND rr.expiryDate > :currentDate")
    List<RewardRedemption> findActiveRedemptions(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount, 
                                                @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find redemptions by booking reference
     */
    List<RewardRedemption> findByBookingReference(String bookingReference);
    
    /**
     * Count redemptions by status
     */
    @Query("SELECT COUNT(rr) FROM RewardRedemption rr WHERE rr.status = :status")
    Long countByStatus(@Param("status") RewardRedemption.RedemptionStatus status);
    
    /**
     * Find recent redemptions by loyalty account
     */
    @Query("SELECT rr FROM RewardRedemption rr WHERE rr.loyaltyAccount = :loyaltyAccount ORDER BY rr.redemptionDate DESC")
    List<RewardRedemption> findRecentRedemptions(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount, 
                                                Pageable pageable);
    
    /**
     * Calculate total points redeemed by loyalty account
     */
    @Query("SELECT COALESCE(SUM(rr.pointsUsed), 0) FROM RewardRedemption rr WHERE rr.loyaltyAccount = :loyaltyAccount")
    java.math.BigDecimal calculateTotalRedeemedPoints(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount);
}
