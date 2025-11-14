package com.irctc.booking.service;

import com.irctc.booking.client.PaymentServiceClient;
import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.*;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.entity.SimplePassenger;
import com.irctc.booking.exception.BusinessException;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling booking modifications
 */
@Service
public class BookingModificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingModificationService.class);
    
    @Autowired
    private SimpleBookingRepository bookingRepository;
    
    @Autowired
    private SimpleBookingService bookingService;
    
    @Autowired
    private ModificationChargeCalculator chargeCalculator;
    
    @Autowired(required = false)
    private TrainServiceClient trainServiceClient;
    
    @Autowired(required = false)
    private PaymentServiceClient paymentServiceClient;
    
    /**
     * Get available modification options for a booking
     */
    @Bulkhead(name = "booking-modification", type = Bulkhead.Type.SEMAPHORE)
    public ModificationOptionsResponse getModificationOptions(Long bookingId) {
        SimpleBooking booking = bookingService.getBookingById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        
        // Validate tenant access
        if (TenantContext.hasTenant() && !TenantContext.getTenantId().equals(booking.getTenantId())) {
            throw new EntityNotFoundException("Booking", bookingId);
        }
        
        // Check if booking can be modified
        if (!canModifyBooking(booking)) {
            throw new BusinessException("Booking cannot be modified. Status: " + booking.getStatus());
        }
        
        ModificationOptionsResponse response = new ModificationOptionsResponse();
        response.setBookingId(bookingId);
        response.setCurrentStatus(booking.getStatus());
        
        // Determine what modifications are allowed
        response.setCanModifyDate(canModifyDate(booking));
        response.setCanUpgradeSeat(canUpgradeSeat(booking));
        response.setCanChangeRoute(canChangeRoute(booking));
        response.setCanModifyPassengers(canModifyPassengers(booking));
        
        // Calculate modification charges
        Map<String, BigDecimal> charges = new HashMap<>();
        if (response.isCanModifyDate()) {
            charges.put("dateChange", chargeCalculator.calculateDateChangeCharge(
                booking.getBookingTime(), LocalDateTime.now()));
        }
        if (response.isCanUpgradeSeat()) {
            charges.put("seatUpgrade", chargeCalculator.calculateSeatUpgradeCharge(
                "CURRENT", "NEW", booking.getBookingTime(), LocalDateTime.now()));
        }
        if (response.isCanChangeRoute()) {
            charges.put("routeChange", chargeCalculator.calculateRouteChangeCharge(
                booking.getBookingTime(), LocalDateTime.now()));
        }
        if (response.isCanModifyPassengers()) {
            charges.put("passengerModification", chargeCalculator.calculatePassengerModificationCharge(
                1, 1, booking.getBookingTime(), LocalDateTime.now()));
        }
        response.setModificationCharges(charges);
        
        return response;
    }
    
    /**
     * Modify booking date
     */
    @Transactional
    @CacheEvict(value = {"bookings", "bookings-by-pnr", "bookings-by-user"}, allEntries = false)
    public ModificationResponse modifyDate(DateChangeRequest request) {
        SimpleBooking booking = bookingService.getBookingById(request.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", request.getBookingId()));
        
        // Validate tenant access
        if (TenantContext.hasTenant() && !TenantContext.getTenantId().equals(booking.getTenantId())) {
            throw new EntityNotFoundException("Booking", request.getBookingId());
        }
        
        // Validate modification is allowed
        if (!canModifyDate(booking)) {
            throw new BusinessException("Date modification is not allowed for this booking");
        }
        
        // Validate new date is in the future
        if (request.getNewJourneyDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("New journey date must be in the future");
        }
        
        // Calculate charges
        BigDecimal modificationCharge = chargeCalculator.calculateDateChangeCharge(
            booking.getBookingTime(), LocalDateTime.now());
        
        // Fetch new fare from Train Service if train changed or date changed
        BigDecimal newFare = booking.getTotalFare();
        Long trainIdToUse = request.getNewTrainId() != null ? request.getNewTrainId() : booking.getTrainId();
        
        if (trainServiceClient != null) {
            try {
                // Get train information
                TrainServiceClient.TrainResponse train = trainServiceClient.getTrainById(trainIdToUse);
                if (train != null && train.getBaseFare() != null) {
                    // Use base fare from train service
                    newFare = BigDecimal.valueOf(train.getBaseFare());
                    
                    // If we have source/destination, calculate actual fare
                    // For now, use base fare multiplied by passenger count
                    int passengerCount = booking.getPassengers() != null ? booking.getPassengers().size() : 1;
                    newFare = newFare.multiply(BigDecimal.valueOf(passengerCount));
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch fare from Train Service, using existing fare: {}", e.getMessage());
            }
        }
        
        BigDecimal fareDifference = chargeCalculator.calculateFareDifference(booking.getTotalFare(), newFare);
        BigDecimal totalAmount = chargeCalculator.calculateTotalAmount(fareDifference, modificationCharge);
        
        // Process payment if amount is positive (user needs to pay)
        String paymentStatus = "PENDING";
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0 && paymentServiceClient != null) {
            try {
                PaymentServiceClient.PaymentRequest paymentRequest = new PaymentServiceClient.PaymentRequest();
                paymentRequest.setBookingId(booking.getId());
                paymentRequest.setAmount(totalAmount);
                paymentRequest.setCurrency("INR");
                paymentRequest.setPaymentMethod("ONLINE");
                paymentRequest.setDescription("Booking modification - Date change");
                paymentRequest.setModificationId("MOD_" + System.currentTimeMillis() + "_" + booking.getId());
                
                PaymentServiceClient.PaymentResponse paymentResponse = paymentServiceClient.processPayment(paymentRequest);
                if (paymentResponse != null && "COMPLETED".equals(paymentResponse.getStatus())) {
                    paymentStatus = "COMPLETED";
                    logger.info("✅ Payment processed for modification: {}", paymentResponse.getTransactionId());
                } else {
                    logger.warn("⚠️ Payment processing failed or pending: {}", 
                        paymentResponse != null ? paymentResponse.getMessage() : "No response");
                }
            } catch (Exception e) {
                logger.error("Failed to process payment for modification: {}", e.getMessage());
            }
        }
        
        // Process refund if amount is negative (user gets refund)
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0 && paymentServiceClient != null) {
            try {
                // Get existing payment for this booking
                List<PaymentServiceClient.PaymentResponse> existingPayments = 
                    paymentServiceClient.getPaymentsByBookingId(booking.getId());
                
                if (!existingPayments.isEmpty()) {
                    PaymentServiceClient.PaymentResponse lastPayment = existingPayments.get(existingPayments.size() - 1);
                    
                    PaymentServiceClient.RefundRequest refundRequest = new PaymentServiceClient.RefundRequest();
                    refundRequest.setPaymentId(lastPayment.getId());
                    refundRequest.setRefundAmount(totalAmount.abs());
                    refundRequest.setReason("Booking modification - Date change refund");
                    refundRequest.setModificationId("MOD_" + System.currentTimeMillis() + "_" + booking.getId());
                    
                    PaymentServiceClient.PaymentResponse refundResponse = paymentServiceClient.processRefund(refundRequest);
                    if (refundResponse != null && "REFUNDED".equals(refundResponse.getStatus())) {
                        paymentStatus = "REFUNDED";
                        logger.info("✅ Refund processed for modification: {}", refundResponse.getTransactionId());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to process refund for modification: {}", e.getMessage());
            }
        }
        
        // Update booking
        booking.setBookingTime(request.getNewJourneyDate());
        if (request.getNewTrainId() != null) {
            booking.setTrainId(request.getNewTrainId());
        }
        booking.setTotalFare(newFare);
        booking.setUpdatedAt(LocalDateTime.now());
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Date modified for booking {}: {} -> {}", 
            booking.getId(), booking.getBookingTime(), request.getNewJourneyDate());
        
        ModificationResponse response = buildModificationResponse(saved, "DATE_CHANGE", fareDifference, modificationCharge, totalAmount);
        response.setRefundStatus(paymentStatus);
        return response;
    }
    
    /**
     * Upgrade or downgrade seat class
     */
    @Transactional
    @CacheEvict(value = {"bookings", "bookings-by-pnr", "bookings-by-user"}, allEntries = false)
    public ModificationResponse upgradeSeat(SeatUpgradeRequest request) {
        SimpleBooking booking = bookingService.getBookingById(request.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", request.getBookingId()));
        
        // Validate tenant access
        if (TenantContext.hasTenant() && !TenantContext.getTenantId().equals(booking.getTenantId())) {
            throw new EntityNotFoundException("Booking", request.getBookingId());
        }
        
        // Validate modification is allowed
        if (!canUpgradeSeat(booking)) {
            throw new BusinessException("Seat upgrade is not allowed for this booking");
        }
        
        // Calculate charges
        BigDecimal modificationCharge = chargeCalculator.calculateSeatUpgradeCharge(
            "CURRENT", request.getNewSeatClass(), booking.getBookingTime(), LocalDateTime.now());
        
        BigDecimal fareDifference = chargeCalculator.calculateFareDifference(
            booking.getTotalFare(), request.getNewFare());
        BigDecimal totalAmount = chargeCalculator.calculateTotalAmount(fareDifference, modificationCharge);
        
        // Process payment/refund
        String paymentStatus = processPaymentForModification(booking.getId(), totalAmount, "Seat upgrade");
        
        // Update booking fare
        booking.setTotalFare(request.getNewFare());
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Update passenger seat numbers if provided
        if (request.getNewSeatNumber() != null && booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            booking.getPassengers().forEach(p -> p.setSeatNumber(request.getNewSeatNumber()));
        }
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Seat upgraded for booking {}: New class {}, New fare {}", 
            booking.getId(), request.getNewSeatClass(), request.getNewFare());
        
        ModificationResponse response = buildModificationResponse(saved, "SEAT_UPGRADE", fareDifference, modificationCharge, totalAmount);
        response.setRefundStatus(paymentStatus);
        return response;
    }
    
    /**
     * Modify passengers (add/remove)
     */
    @Transactional
    @CacheEvict(value = {"bookings", "bookings-by-pnr", "bookings-by-user"}, allEntries = false)
    public ModificationResponse modifyPassengers(PassengerModificationRequest request) {
        SimpleBooking booking = bookingService.getBookingById(request.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", request.getBookingId()));
        
        // Validate tenant access
        if (TenantContext.hasTenant() && !TenantContext.getTenantId().equals(booking.getTenantId())) {
            throw new EntityNotFoundException("Booking", request.getBookingId());
        }
        
        // Validate modification is allowed
        if (!canModifyPassengers(booking)) {
            throw new BusinessException("Passenger modification is not allowed for this booking");
        }
        
        // Remove passengers
        if (request.getPassengerIdsToRemove() != null && !request.getPassengerIdsToRemove().isEmpty()) {
            List<SimplePassenger> passengersToRemove = booking.getPassengers().stream()
                .filter(p -> request.getPassengerIdsToRemove().contains(p.getId()))
                .collect(Collectors.toList());
            
            if (passengersToRemove.size() != request.getPassengerIdsToRemove().size()) {
                throw new BusinessException("Some passenger IDs not found in booking");
            }
            
            booking.getPassengers().removeAll(passengersToRemove);
        }
        
        // Add passengers
        int passengersAdded = 0;
        if (request.getPassengersToAdd() != null && !request.getPassengersToAdd().isEmpty()) {
            for (SimplePassenger newPassenger : request.getPassengersToAdd()) {
                newPassenger.setId(null); // Ensure new entity
                if (TenantContext.hasTenant()) {
                    newPassenger.setTenantId(TenantContext.getTenantId());
                }
                booking.getPassengers().add(newPassenger);
                passengersAdded++;
            }
        }
        
        // Calculate charges
        int passengersRemoved = request.getPassengerIdsToRemove() != null ? 
            request.getPassengerIdsToRemove().size() : 0;
        BigDecimal modificationCharge = chargeCalculator.calculatePassengerModificationCharge(
            passengersAdded, passengersRemoved, booking.getBookingTime(), LocalDateTime.now());
        
        // Update fare
        BigDecimal additionalFare = request.getAdditionalFare() != null ? 
            request.getAdditionalFare() : BigDecimal.ZERO;
        BigDecimal newFare = booking.getTotalFare().add(additionalFare);
        booking.setTotalFare(newFare);
        booking.setUpdatedAt(LocalDateTime.now());
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Passengers modified for booking {}: Added {}, Removed {}", 
            booking.getId(), passengersAdded, passengersRemoved);
        
        BigDecimal fareDifference = additionalFare;
        BigDecimal totalAmount = chargeCalculator.calculateTotalAmount(fareDifference, modificationCharge);
        
        // Process payment/refund
        String paymentStatus = processPaymentForModification(booking.getId(), totalAmount, "Passenger modification");
        
        ModificationResponse response = buildModificationResponse(saved, "PASSENGER_MODIFICATION", fareDifference, modificationCharge, totalAmount);
        response.setRefundStatus(paymentStatus);
        return response;
    }
    
    /**
     * Change route (source/destination)
     */
    @Transactional
    @CacheEvict(value = {"bookings", "bookings-by-pnr", "bookings-by-user"}, allEntries = false)
    public ModificationResponse changeRoute(RouteChangeRequest request) {
        SimpleBooking booking = bookingService.getBookingById(request.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", request.getBookingId()));
        
        // Validate tenant access
        if (TenantContext.hasTenant() && !TenantContext.getTenantId().equals(booking.getTenantId())) {
            throw new EntityNotFoundException("Booking", request.getBookingId());
        }
        
        // Validate modification is allowed
        if (!canChangeRoute(booking)) {
            throw new BusinessException("Route change is not allowed for this booking");
        }
        
        // Calculate charges
        BigDecimal modificationCharge = chargeCalculator.calculateRouteChangeCharge(
            booking.getBookingTime(), LocalDateTime.now());
        
        BigDecimal fareDifference = chargeCalculator.calculateFareDifference(
            booking.getTotalFare(), request.getNewFare());
        BigDecimal totalAmount = chargeCalculator.calculateTotalAmount(fareDifference, modificationCharge);
        
        // Process payment/refund
        String paymentStatus = processPaymentForModification(booking.getId(), totalAmount, "Route change");
        
        // Update booking
        if (request.getNewTrainId() != null) {
            booking.setTrainId(request.getNewTrainId());
        }
        booking.setTotalFare(request.getNewFare());
        booking.setUpdatedAt(LocalDateTime.now());
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Route changed for booking {}: {} -> {}", 
            booking.getId(), booking.getBookingTime(), request.getNewSourceStation() + " to " + request.getNewDestinationStation());
        
        ModificationResponse response = buildModificationResponse(saved, "ROUTE_CHANGE", fareDifference, modificationCharge, totalAmount);
        response.setRefundStatus(paymentStatus);
        return response;
    }
    
    /**
     * Check if booking can be modified
     */
    private boolean canModifyBooking(SimpleBooking booking) {
        return "CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus());
    }
    
    /**
     * Check if date can be modified
     */
    private boolean canModifyDate(SimpleBooking booking) {
        if (!canModifyBooking(booking)) {
            return false;
        }
        // Can modify if journey is at least 4 hours away
        return booking.getBookingTime().isAfter(LocalDateTime.now().plusHours(4));
    }
    
    /**
     * Check if seat can be upgraded
     */
    private boolean canUpgradeSeat(SimpleBooking booking) {
        if (!canModifyBooking(booking)) {
            return false;
        }
        // Can upgrade if journey is at least 2 hours away
        return booking.getBookingTime().isAfter(LocalDateTime.now().plusHours(2));
    }
    
    /**
     * Check if route can be changed
     */
    private boolean canChangeRoute(SimpleBooking booking) {
        if (!canModifyBooking(booking)) {
            return false;
        }
        // Can change route if journey is at least 6 hours away
        return booking.getBookingTime().isAfter(LocalDateTime.now().plusHours(6));
    }
    
    /**
     * Check if passengers can be modified
     */
    private boolean canModifyPassengers(SimpleBooking booking) {
        if (!canModifyBooking(booking)) {
            return false;
        }
        // Can modify passengers if journey is at least 4 hours away
        return booking.getBookingTime().isAfter(LocalDateTime.now().plusHours(4));
    }
    
    /**
     * Process payment or refund for modification
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "processPaymentFallback")
    String processPaymentForModification(Long bookingId, BigDecimal totalAmount, String description) {
        if (paymentServiceClient == null) {
            return "PAYMENT_SERVICE_UNAVAILABLE";
        }
        
        try {
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Process payment
                PaymentServiceClient.PaymentRequest paymentRequest = new PaymentServiceClient.PaymentRequest();
                paymentRequest.setBookingId(bookingId);
                paymentRequest.setAmount(totalAmount);
                paymentRequest.setCurrency("INR");
                paymentRequest.setPaymentMethod("ONLINE");
                paymentRequest.setDescription("Booking modification - " + description);
                paymentRequest.setModificationId("MOD_" + System.currentTimeMillis() + "_" + bookingId);
                
                PaymentServiceClient.PaymentResponse paymentResponse = paymentServiceClient.processPayment(paymentRequest);
                if (paymentResponse != null && "COMPLETED".equals(paymentResponse.getStatus())) {
                    logger.info("✅ Payment processed for modification: {}", paymentResponse.getTransactionId());
                    return "COMPLETED";
                }
                return paymentResponse != null ? paymentResponse.getStatus() : "PENDING";
            } else if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                // Process refund
                List<PaymentServiceClient.PaymentResponse> existingPayments = 
                    paymentServiceClient.getPaymentsByBookingId(bookingId);
                
                if (!existingPayments.isEmpty()) {
                    PaymentServiceClient.PaymentResponse lastPayment = existingPayments.get(existingPayments.size() - 1);
                    
                    PaymentServiceClient.RefundRequest refundRequest = new PaymentServiceClient.RefundRequest();
                    refundRequest.setPaymentId(lastPayment.getId());
                    refundRequest.setRefundAmount(totalAmount.abs());
                    refundRequest.setReason("Booking modification - " + description + " refund");
                    refundRequest.setModificationId("MOD_" + System.currentTimeMillis() + "_" + bookingId);
                    
                    PaymentServiceClient.PaymentResponse refundResponse = paymentServiceClient.processRefund(refundRequest);
                    if (refundResponse != null && "REFUNDED".equals(refundResponse.getStatus())) {
                        logger.info("✅ Refund processed for modification: {}", refundResponse.getTransactionId());
                        return "REFUNDED";
                    }
                    return refundResponse != null ? refundResponse.getStatus() : "PENDING";
                }
            }
            return "NO_PAYMENT_REQUIRED";
        } catch (Exception e) {
            logger.error("Failed to process payment/refund for modification: {}", e.getMessage());
            return "FAILED";
        }
    }
    
    /**
     * Fallback for payment processing
     */
    private String processPaymentFallback(Long bookingId, BigDecimal totalAmount, String description, Exception e) {
        logger.warn("Payment processing fallback triggered: {}", e.getMessage());
        return "PAYMENT_SERVICE_UNAVAILABLE";
    }
    
    /**
     * Build modification response
     */
    private ModificationResponse buildModificationResponse(SimpleBooking booking, String modificationType,
                                                           BigDecimal fareDifference, BigDecimal modificationCharge,
                                                           BigDecimal totalAmount) {
        ModificationResponse response = new ModificationResponse();
        response.setBookingId(booking.getId());
        response.setPnrNumber(booking.getPnrNumber());
        response.setModificationType(modificationType);
        response.setStatus("SUCCESS");
        response.setMessage("Booking modified successfully");
        response.setModifiedBooking(booking);
        
        response.setOriginalFare(booking.getTotalFare().subtract(fareDifference));
        response.setNewFare(booking.getTotalFare());
        response.setFareDifference(fareDifference);
        response.setModificationCharge(modificationCharge);
        response.setTotalAmount(totalAmount);
        
        response.setModificationDate(LocalDateTime.now());
        response.setModificationId("MOD_" + System.currentTimeMillis() + "_" + booking.getId());
        
        // If total amount is negative, it's a refund
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            response.setRefundAmount(totalAmount.abs());
            response.setRefundStatus("PENDING");
        }
        
        return response;
    }
}

