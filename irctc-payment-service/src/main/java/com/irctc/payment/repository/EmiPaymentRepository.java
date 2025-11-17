package com.irctc.payment.repository;

import com.irctc.payment.entity.EmiPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiPaymentRepository extends JpaRepository<EmiPayment, Long> {
    List<EmiPayment> findByPaymentPlanId(Long paymentPlanId);
}

