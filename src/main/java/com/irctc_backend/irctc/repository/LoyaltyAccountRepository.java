package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.LoyaltyAccount;
import com.irctc_backend.irctc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoyaltyAccount entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    
    /**
     * Find loyalty account by user
     */
    Optional<LoyaltyAccount> findByUser(User user);
    
    /**
     * Find loyalty account by user ID
     */
    Optional<LoyaltyAccount> findByUserId(Long userId);
    
    /**
     * Find loyalty account by loyalty number
     */
    Optional<LoyaltyAccount> findByLoyaltyNumber(String loyaltyNumber);
    
    /**
     * Find active loyalty accounts
     */
    List<LoyaltyAccount> findByIsActiveTrue();
    
    /**
     * Find loyalty accounts by tier
     */
    List<LoyaltyAccount> findByTier(LoyaltyAccount.LoyaltyTier tier);
    
    /**
     * Find loyalty accounts with points expiring soon
     */
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.pointsExpiryDate <= :expiryDate AND la.availablePoints > 0")
    List<LoyaltyAccount> findAccountsWithExpiringPoints(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find top loyalty accounts by total points
     */
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.isActive = true ORDER BY la.totalPoints DESC")
    List<LoyaltyAccount> findTopAccountsByPoints();
    
    /**
     * Find loyalty accounts by total spent range
     */
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.totalSpent BETWEEN :minSpent AND :maxSpent AND la.isActive = true")
    List<LoyaltyAccount> findAccountsBySpentRange(@Param("minSpent") java.math.BigDecimal minSpent, 
                                                 @Param("maxSpent") java.math.BigDecimal maxSpent);
    
    /**
     * Count loyalty accounts by tier
     */
    @Query("SELECT COUNT(la) FROM LoyaltyAccount la WHERE la.tier = :tier AND la.isActive = true")
    Long countByTier(@Param("tier") LoyaltyAccount.LoyaltyTier tier);
    
    /**
     * Find loyalty accounts created in date range
     */
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.joinedDate BETWEEN :startDate AND :endDate")
    List<LoyaltyAccount> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find loyalty accounts with no activity for specified days
     */
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.lastActivityDate < :cutoffDate AND la.isActive = true")
    List<LoyaltyAccount> findInactiveAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
