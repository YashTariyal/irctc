package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.LoyaltyAccount;
import com.irctc_backend.irctc.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for LoyaltyTransaction entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    
    /**
     * Find transactions by loyalty account
     */
    List<LoyaltyTransaction> findByLoyaltyAccount(LoyaltyAccount loyaltyAccount);
    
    /**
     * Find transactions by loyalty account with pagination
     */
    Page<LoyaltyTransaction> findByLoyaltyAccount(LoyaltyAccount loyaltyAccount, Pageable pageable);
    
    /**
     * Find transactions by transaction type
     */
    List<LoyaltyTransaction> findByTransactionType(LoyaltyTransaction.TransactionType transactionType);
    
    /**
     * Find transactions by reference type
     */
    List<LoyaltyTransaction> findByReferenceType(LoyaltyTransaction.ReferenceType referenceType);
    
    /**
     * Find transactions by reference ID
     */
    List<LoyaltyTransaction> findByReferenceId(String referenceId);
    
    /**
     * Find expired transactions
     */
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.expiryDate <= :currentDate AND lt.isExpired = false")
    List<LoyaltyTransaction> findExpiredTransactions(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find transactions created in date range
     */
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findTransactionsBetween(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transactions by loyalty account and transaction type
     */
    List<LoyaltyTransaction> findByLoyaltyAccountAndTransactionType(LoyaltyAccount loyaltyAccount, 
                                                                   LoyaltyTransaction.TransactionType transactionType);
    
    /**
     * Find transactions by loyalty account and reference type
     */
    List<LoyaltyTransaction> findByLoyaltyAccountAndReferenceType(LoyaltyAccount loyaltyAccount, 
                                                                 LoyaltyTransaction.ReferenceType referenceType);
    
    /**
     * Calculate total points earned by loyalty account
     */
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.loyaltyAccount = :loyaltyAccount AND lt.transactionType = 'EARNED'")
    java.math.BigDecimal calculateTotalEarnedPoints(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount);
    
    /**
     * Calculate total points redeemed by loyalty account
     */
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.loyaltyAccount = :loyaltyAccount AND lt.transactionType = 'REDEEMED'")
    java.math.BigDecimal calculateTotalRedeemedPoints(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount);
    
    /**
     * Find recent transactions by loyalty account
     */
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.loyaltyAccount = :loyaltyAccount ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findRecentTransactions(@Param("loyaltyAccount") LoyaltyAccount loyaltyAccount, 
                                                   Pageable pageable);
}
