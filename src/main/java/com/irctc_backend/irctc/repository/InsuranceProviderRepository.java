package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for InsuranceProvider entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, Long> {
    
    /**
     * Find active insurance providers
     */
    List<InsuranceProvider> findByIsActiveTrue();
    
    /**
     * Find featured insurance providers
     */
    List<InsuranceProvider> findByIsFeaturedTrueAndIsActiveTrue();
    
    /**
     * Find providers by rating range
     */
    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.rating BETWEEN :minRating AND :maxRating AND ip.isActive = true")
    List<InsuranceProvider> findByRatingRange(@Param("minRating") java.math.BigDecimal minRating, 
                                             @Param("maxRating") java.math.BigDecimal maxRating);
    
    /**
     * Find providers sorted by rating
     */
    List<InsuranceProvider> findByIsActiveTrueOrderByRatingDesc();
    
    /**
     * Find providers sorted by total policies sold
     */
    List<InsuranceProvider> findByIsActiveTrueOrderByTotalPoliciesSoldDesc();
    
    /**
     * Find providers by claim settlement ratio
     */
    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.claimSettlementRatio >= :minRatio AND ip.isActive = true")
    List<InsuranceProvider> findByClaimSettlementRatioGreaterThanEqual(@Param("minRatio") java.math.BigDecimal minRatio);
    
    /**
     * Find providers by average settlement days
     */
    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.averageSettlementDays <= :maxDays AND ip.isActive = true")
    List<InsuranceProvider> findByAverageSettlementDaysLessThanEqual(@Param("maxDays") Integer maxDays);
    
    /**
     * Find providers by coverage amount range
     */
    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.minCoverageAmount <= :coverageAmount AND ip.maxCoverageAmount >= :coverageAmount AND ip.isActive = true")
    List<InsuranceProvider> findByCoverageAmountRange(@Param("coverageAmount") java.math.BigDecimal coverageAmount);
    
    /**
     * Find top providers by rating and policies sold
     */
    @Query("SELECT ip FROM InsuranceProvider ip WHERE ip.isActive = true ORDER BY ip.rating DESC, ip.totalPoliciesSold DESC")
    List<InsuranceProvider> findTopProviders();
    
    /**
     * Count active providers
     */
    Long countByIsActiveTrue();
}
