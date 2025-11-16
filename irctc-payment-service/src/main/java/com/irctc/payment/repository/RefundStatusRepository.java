package com.irctc.payment.repository;

import com.irctc.payment.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundStatusRepository extends JpaRepository<RefundStatus, Long> {
    
    List<RefundStatus> findByPaymentId(Long paymentId);
    
    List<RefundStatus> findByBookingId(Long bookingId);
    
    Optional<RefundStatus> findByRefundId(String refundId);
    
    Optional<RefundStatus> findByGatewayRefundId(String gatewayRefundId);
    
    List<RefundStatus> findByStatus(String status);
    
    @Query("SELECT r FROM RefundStatus r WHERE r.paymentId = :paymentId AND r.status IN :statuses")
    List<RefundStatus> findByPaymentIdAndStatusIn(@Param("paymentId") Long paymentId, 
                                                   @Param("statuses") List<String> statuses);
    
    @Query("SELECT r FROM RefundStatus r WHERE r.reconciliationStatus = :reconciliationStatus")
    List<RefundStatus> findByReconciliationStatus(@Param("reconciliationStatus") String reconciliationStatus);
}

