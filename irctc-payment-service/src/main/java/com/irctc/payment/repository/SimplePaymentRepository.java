package com.irctc.payment.repository;

import com.irctc.payment.entity.SimplePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimplePaymentRepository extends JpaRepository<SimplePayment, Long> {
    Optional<SimplePayment> findByTransactionId(String transactionId);
    List<SimplePayment> findByBookingId(Long bookingId);
}
