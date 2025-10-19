package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.InsurancePlan;
import com.irctc_backend.irctc.entity.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for InsurancePlan entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {
    
    /**
     * Find active insurance plans
     */
    List<InsurancePlan> findByIsActiveTrue();
    
    /**
     * Find plans by provider
     */
    List<InsurancePlan> findByProvider(InsuranceProvider provider);
    
    /**
     * Find active plans by provider
     */
    List<InsurancePlan> findByProviderAndIsActiveTrue(InsuranceProvider provider);
    
    /**
     * Find plans by plan type
     */
    List<InsurancePlan> findByPlanType(InsurancePlan.PlanType planType);
    
    /**
     * Find active plans by plan type
     */
    List<InsurancePlan> findByPlanTypeAndIsActiveTrue(InsurancePlan.PlanType planType);
    
    /**
     * Find featured plans
     */
    List<InsurancePlan> findByIsFeaturedTrueAndIsActiveTrue();
    
    /**
     * Find plans by age eligibility
     */
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.ageMin <= :age AND ip.ageMax >= :age AND ip.isActive = true")
    List<InsurancePlan> findByAgeEligibility(@Param("age") Integer age);
    
    /**
     * Find plans by coverage amount range
     */
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.minCoverageAmount <= :coverageAmount AND ip.maxCoverageAmount >= :coverageAmount AND ip.isActive = true")
    List<InsurancePlan> findByCoverageAmountRange(@Param("coverageAmount") BigDecimal coverageAmount);
    
    /**
     * Find plans by premium rate range
     */
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.premiumRate BETWEEN :minRate AND :maxRate AND ip.isActive = true")
    List<InsurancePlan> findByPremiumRateRange(@Param("minRate") BigDecimal minRate, 
                                              @Param("maxRate") BigDecimal maxRate);
    
    /**
     * Find plans sorted by popularity
     */
    List<InsurancePlan> findByIsActiveTrueOrderByPopularityScoreDesc();
    
    /**
     * Find plans sorted by premium rate
     */
    List<InsurancePlan> findByIsActiveTrueOrderByPremiumRateAsc();
    
    /**
     * Find plans that cover specific benefits
     */
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.coversMedicalExpenses = :coversMedical AND ip.isActive = true")
    List<InsurancePlan> findByMedicalCoverage(@Param("coversMedical") Boolean coversMedical);
    
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.coversTripCancellation = :coversCancellation AND ip.isActive = true")
    List<InsurancePlan> findByTripCancellationCoverage(@Param("coversCancellation") Boolean coversCancellation);
    
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.coversBaggageLoss = :coversBaggage AND ip.isActive = true")
    List<InsurancePlan> findByBaggageCoverage(@Param("coversBaggage") Boolean coversBaggage);
    
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.coversPersonalAccident = :coversAccident AND ip.isActive = true")
    List<InsurancePlan> findByPersonalAccidentCoverage(@Param("coversAccident") Boolean coversAccident);
    
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.coversEmergencyEvacuation = :coversEvacuation AND ip.isActive = true")
    List<InsurancePlan> findByEmergencyEvacuationCoverage(@Param("coversEvacuation") Boolean coversEvacuation);
    
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.covers24x7Support = :coversSupport AND ip.isActive = true")
    List<InsurancePlan> findBy24x7SupportCoverage(@Param("coversSupport") Boolean coversSupport);
    
    /**
     * Find plans by provider and plan type
     */
    List<InsurancePlan> findByProviderAndPlanTypeAndIsActiveTrue(InsuranceProvider provider, 
                                                               InsurancePlan.PlanType planType);
    
    /**
     * Find plans by multiple criteria
     */
    @Query("SELECT ip FROM InsurancePlan ip WHERE ip.provider = :provider AND ip.planType = :planType AND " +
           "ip.ageMin <= :age AND ip.ageMax >= :age AND " +
           "ip.minCoverageAmount <= :coverageAmount AND ip.maxCoverageAmount >= :coverageAmount AND " +
           "ip.isActive = true")
    List<InsurancePlan> findByMultipleCriteria(@Param("provider") InsuranceProvider provider,
                                              @Param("planType") InsurancePlan.PlanType planType,
                                              @Param("age") Integer age,
                                              @Param("coverageAmount") BigDecimal coverageAmount);
    
    /**
     * Count plans by provider
     */
    Long countByProviderAndIsActiveTrue(InsuranceProvider provider);
    
    /**
     * Count plans by plan type
     */
    Long countByPlanTypeAndIsActiveTrue(InsurancePlan.PlanType planType);
}
