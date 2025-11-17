package com.irctc.payment.repository;

import com.irctc.payment.entity.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    Optional<PaymentPlan> findByBookingId(Long bookingId);
    List<PaymentPlan> findByUserId(Long userId);
}

