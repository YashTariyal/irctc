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
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM SimplePayment p WHERE p.paymentTime BETWEEN :start AND :end")
    java.util.List<SimplePayment> findByPaymentTimeBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
    
    List<SimplePayment> findByGatewayName(String gatewayName);
    List<SimplePayment> findByStatus(String status);
}
