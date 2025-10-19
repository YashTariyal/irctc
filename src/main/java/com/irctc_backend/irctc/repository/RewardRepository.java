package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Reward entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    /**
     * Find active rewards
     */
    List<Reward> findByIsActiveTrue();
    
    /**
     * Find rewards by category
     */
    List<Reward> findByCategory(Reward.RewardCategory category);
    
    /**
     * Find active rewards by category
     */
    List<Reward> findByCategoryAndIsActiveTrue(Reward.RewardCategory category);
    
    /**
     * Find featured rewards
     */
    List<Reward> findByIsFeaturedTrueAndIsActiveTrue();
    
    /**
     * Find rewards by minimum tier required
     */
    List<Reward> findByMinTierRequiredAndIsActiveTrue(String minTierRequired);
    
    /**
     * Find rewards by points range
     */
    @Query("SELECT r FROM Reward r WHERE r.pointsRequired BETWEEN :minPoints AND :maxPoints AND r.isActive = true")
    List<Reward> findRewardsByPointsRange(@Param("minPoints") BigDecimal minPoints, 
                                         @Param("maxPoints") BigDecimal maxPoints);
    
    /**
     * Find rewards available for tier
     */
    @Query("SELECT r FROM Reward r WHERE r.isActive = true AND " +
           "(r.minTierRequired = 'BRONZE' OR " +
           "(r.minTierRequired = 'SILVER' AND :tier IN ('SILVER', 'GOLD', 'PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'GOLD' AND :tier IN ('GOLD', 'PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'PLATINUM' AND :tier IN ('PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'DIAMOND' AND :tier = 'DIAMOND'))")
    List<Reward> findRewardsAvailableForTier(@Param("tier") String tier);
    
    /**
     * Find rewards with available redemption limit
     */
    @Query("SELECT r FROM Reward r WHERE r.isActive = true AND " +
           "(r.redemptionLimit IS NULL OR r.redemptionCount < r.redemptionLimit)")
    List<Reward> findRewardsWithAvailableLimit();
    
    /**
     * Find rewards by category and tier
     */
    @Query("SELECT r FROM Reward r WHERE r.category = :category AND r.isActive = true AND " +
           "(r.minTierRequired = 'BRONZE' OR " +
           "(r.minTierRequired = 'SILVER' AND :tier IN ('SILVER', 'GOLD', 'PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'GOLD' AND :tier IN ('GOLD', 'PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'PLATINUM' AND :tier IN ('PLATINUM', 'DIAMOND')) OR " +
           "(r.minTierRequired = 'DIAMOND' AND :tier = 'DIAMOND'))")
    List<Reward> findRewardsByCategoryAndTier(@Param("category") Reward.RewardCategory category, 
                                             @Param("tier") String tier);
    
    /**
     * Find rewards sorted by points required
     */
    List<Reward> findByIsActiveTrueOrderByPointsRequiredAsc();
    
    /**
     * Find rewards sorted by popularity (redemption count)
     */
    List<Reward> findByIsActiveTrueOrderByRedemptionCountDesc();
}
