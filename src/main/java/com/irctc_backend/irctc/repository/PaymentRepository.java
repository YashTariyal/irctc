package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Booking;
import com.irctc_backend.irctc.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 * 
 * This repository provides data access methods for Payment entities
 * with custom queries for payment processing and analytics.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by transaction ID
     * 
     * @param transactionId Transaction ID
     * @return Optional Payment
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find payment by gateway order ID
     * 
     * @param gatewayOrderId Gateway order ID
     * @return Optional Payment
     */
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    
    /**
     * Find payment by gateway payment ID
     * 
     * @param gatewayPaymentId Gateway payment ID
     * @return Optional Payment
     */
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    
    /**
     * Find all payments for a specific booking
     * 
     * @param booking Booking entity
     * @return List of payments
     */
    List<Payment> findByBooking(Booking booking);
    
    /**
     * Find all payments by status
     * 
     * @param status Payment status
     * @return List of payments
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    /**
     * Find all payments by payment method
     * 
     * @param paymentMethod Payment method
     * @return List of payments
     */
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    /**
     * Find payments that need retry (failed payments within retry limit)
     * 
     * @param currentTime Current time
     * @return List of payments that need retry
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' " +
           "AND p.retryCount < p.maxRetryAttempts " +
           "AND (p.nextRetryAt IS NULL OR p.nextRetryAt <= :currentTime)")
    List<Payment> findPaymentsNeedingRetry(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find pending payments older than specified time
     * 
     * @param cutoffTime Cutoff time
     * @return List of old pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' " +
           "AND p.createdAt <= :cutoffTime")
    List<Payment> findOldPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count payments by status for a specific date range
     * 
     * @param status Payment status
     * @param startDate Start date
     * @param endDate End date
     * @return Count of payments
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total amount by status for a specific date range
     * 
     * @param status Payment status
     * @param startDate Start date
     * @param endDate End date
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    Double sumAmountByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find payments by booking ID and status
     * 
     * @param bookingId Booking ID
     * @param status Payment status
     * @return List of payments
     */
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.status = :status")
    List<Payment> findByBookingIdAndStatus(@Param("bookingId") Long bookingId,
                                          @Param("status") Payment.PaymentStatus status);
    
    /**
     * Check if a successful payment exists for a booking
     * 
     * @param bookingId Booking ID
     * @return true if successful payment exists
     */
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.booking.id = :bookingId " +
           "AND p.status = 'COMPLETED'")
    boolean existsSuccessfulPaymentForBooking(@Param("bookingId") Long bookingId);
    
    /**
     * Find the latest payment for a booking
     * 
     * @param bookingId Booking ID
     * @return Optional Payment
     */
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId " +
           "ORDER BY p.createdAt DESC")
    Optional<Payment> findLatestPaymentForBooking(@Param("bookingId") Long bookingId);
}
