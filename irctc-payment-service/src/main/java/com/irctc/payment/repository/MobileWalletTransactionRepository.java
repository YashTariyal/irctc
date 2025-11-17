package com.irctc.payment.repository;

import com.irctc.payment.entity.MobileWalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobileWalletTransactionRepository extends JpaRepository<MobileWalletTransaction, Long> {
    Optional<MobileWalletTransaction> findByWalletReference(String walletReference);
}

