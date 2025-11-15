package com.irctc.payment.repository;

import com.irctc.payment.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    /**
     * Find transactions by wallet ID
     */
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    /**
     * Find transactions by wallet ID with pagination
     */
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    /**
     * Find transactions by user ID
     */
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find transactions by user ID with pagination
     */
    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find transactions by type
     */
    List<WalletTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
        String userId, 
        WalletTransaction.TransactionType transactionType
    );
    
    /**
     * Find transactions by date range
     */
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.userId = :userId " +
           "AND wt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transaction by transaction ID
     */
    Optional<WalletTransaction> findByTransactionId(String transactionId);
    
    /**
     * Find transactions by reference
     */
    List<WalletTransaction> findByReferenceIdAndReferenceType(
        String referenceId, 
        String referenceType
    );
}

