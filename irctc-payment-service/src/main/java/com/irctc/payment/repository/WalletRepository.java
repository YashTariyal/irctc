package com.irctc.payment.repository;

import com.irctc.payment.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    /**
     * Find wallet by user ID with optimistic lock
     */
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Wallet> findByUserId(String userId);
    
    /**
     * Find wallet by user ID without lock (for read-only operations)
     */
    Optional<Wallet> findFirstByUserId(String userId);
    
    /**
     * Check if wallet exists for user
     */
    boolean existsByUserId(String userId);
    
    /**
     * Find active wallets
     */
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.isActive = true")
    Optional<Wallet> findActiveWalletByUserId(@Param("userId") String userId);
}

