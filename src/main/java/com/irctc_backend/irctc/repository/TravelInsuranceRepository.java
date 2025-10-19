package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.TravelInsurance;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TravelInsurance entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface TravelInsuranceRepository extends JpaRepository<TravelInsurance, Long> {
    
    /**
     * Find insurance by policy number
     */
    Optional<TravelInsurance> findByPolicyNumber(String policyNumber);
    
    /**
     * Find insurance by user
     */
    List<TravelInsurance> findByUser(User user);
    
    /**
     * Find insurance by user with pagination
     */
    Page<TravelInsurance> findByUser(User user, Pageable pageable);
    
    /**
     * Find insurance by booking
     */
    List<TravelInsurance> findByBooking(Booking booking);
    
    /**
     * Find insurance by policy status
     */
    List<TravelInsurance> findByPolicyStatus(TravelInsurance.PolicyStatus policyStatus);
    
    /**
     * Find insurance by payment status
     */
    List<TravelInsurance> findByPaymentStatus(TravelInsurance.PaymentStatus paymentStatus);
    
    /**
     * Find insurance by user and policy status
     */
    List<TravelInsurance> findByUserAndPolicyStatus(User user, TravelInsurance.PolicyStatus policyStatus);
    
    /**
     * Find insurance by user and payment status
     */
    List<TravelInsurance> findByUserAndPaymentStatus(User user, TravelInsurance.PaymentStatus paymentStatus);
    
    /**
     * Find active insurance policies
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.policyStatus = 'ACTIVE' AND ti.coverageEndDate >= :currentDate")
    List<TravelInsurance> findActivePolicies(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find expired insurance policies
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.policyStatus = 'ACTIVE' AND ti.coverageEndDate < :currentDate")
    List<TravelInsurance> findExpiredPolicies(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find insurance policies expiring soon
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.policyStatus = 'ACTIVE' AND ti.coverageEndDate BETWEEN :startDate AND :endDate")
    List<TravelInsurance> findPoliciesExpiringBetween(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    /**
     * Find insurance policies created in date range
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.purchaseDate BETWEEN :startDate AND :endDate")
    List<TravelInsurance> findPoliciesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find insurance policies by journey date range
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.journeyStartDate BETWEEN :startDate AND :endDate")
    List<TravelInsurance> findPoliciesByJourneyDateRange(@Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);
    
    /**
     * Find insurance policies by coverage amount range
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.coverageAmount BETWEEN :minAmount AND :maxAmount")
    List<TravelInsurance> findPoliciesByCoverageAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                           @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    /**
     * Find insurance policies by premium amount range
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.premiumAmount BETWEEN :minAmount AND :maxAmount")
    List<TravelInsurance> findPoliciesByPremiumAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                          @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    /**
     * Find insurance policies by train number
     */
    List<TravelInsurance> findByTrainNumber(String trainNumber);
    
    /**
     * Find insurance policies by PNR number
     */
    List<TravelInsurance> findByPnrNumber(String pnrNumber);
    
    /**
     * Find insurance policies by source and destination stations
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.sourceStation = :sourceStation AND ti.destinationStation = :destinationStation")
    List<TravelInsurance> findBySourceAndDestinationStations(@Param("sourceStation") String sourceStation, 
                                                            @Param("destinationStation") String destinationStation);
    
    /**
     * Find recent insurance policies by user
     */
    @Query("SELECT ti FROM TravelInsurance ti WHERE ti.user = :user ORDER BY ti.purchaseDate DESC")
    List<TravelInsurance> findRecentPoliciesByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Count insurance policies by user
     */
    Long countByUser(User user);
    
    /**
     * Count insurance policies by policy status
     */
    Long countByPolicyStatus(TravelInsurance.PolicyStatus policyStatus);
    
    /**
     * Count insurance policies by payment status
     */
    Long countByPaymentStatus(TravelInsurance.PaymentStatus paymentStatus);
    
    /**
     * Calculate total coverage amount by user
     */
    @Query("SELECT COALESCE(SUM(ti.coverageAmount), 0) FROM TravelInsurance ti WHERE ti.user = :user AND ti.policyStatus = 'ACTIVE'")
    java.math.BigDecimal calculateTotalCoverageByUser(@Param("user") User user);
    
    /**
     * Calculate total premium paid by user
     */
    @Query("SELECT COALESCE(SUM(ti.totalAmount), 0) FROM TravelInsurance ti WHERE ti.user = :user AND ti.paymentStatus = 'PAID'")
    java.math.BigDecimal calculateTotalPremiumPaidByUser(@Param("user") User user);
}
