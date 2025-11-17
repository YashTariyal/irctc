package com.irctc.payment.repository;

import com.irctc.payment.entity.UpiPaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpiPaymentIntentRepository extends JpaRepository<UpiPaymentIntent, Long> {
    Optional<UpiPaymentIntent> findByOrderId(String orderId);
}

