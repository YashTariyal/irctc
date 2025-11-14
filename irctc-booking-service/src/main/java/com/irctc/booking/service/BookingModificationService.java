package com.irctc.booking.service;

import com.irctc.booking.dto.*;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.entity.SimplePassenger;
import com.irctc.booking.exception.BusinessException;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        
        // For now, assume fare remains same (in real implementation, fetch from Train Service)
        BigDecimal newFare = booking.getTotalFare();
        if (request.getNewTrainId() != null && !request.getNewTrainId().equals(booking.getTrainId())) {
            // Train changed - would need to fetch new fare from Train Service
            // For now, keep same fare
            booking.setTrainId(request.getNewTrainId());
        }
        
        BigDecimal fareDifference = chargeCalculator.calculateFareDifference(booking.getTotalFare(), newFare);
        BigDecimal totalAmount = chargeCalculator.calculateTotalAmount(fareDifference, modificationCharge);
        
        // Update booking
        booking.setBookingTime(request.getNewJourneyDate());
        booking.setTotalFare(newFare);
        booking.setUpdatedAt(LocalDateTime.now());
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Date modified for booking {}: {} -> {}", 
            booking.getId(), booking.getBookingTime(), request.getNewJourneyDate());
        
        return buildModificationResponse(saved, "DATE_CHANGE", fareDifference, modificationCharge, totalAmount);
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
        
        return buildModificationResponse(saved, "SEAT_UPGRADE", fareDifference, modificationCharge, totalAmount);
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
        
        return buildModificationResponse(saved, "PASSENGER_MODIFICATION", fareDifference, modificationCharge, totalAmount);
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
        
        // Update booking
        if (request.getNewTrainId() != null) {
            booking.setTrainId(request.getNewTrainId());
        }
        booking.setTotalFare(request.getNewFare());
        booking.setUpdatedAt(LocalDateTime.now());
        
        SimpleBooking saved = bookingRepository.save(booking);
        
        logger.info("✅ Route changed for booking {}: {} -> {}", 
            booking.getId(), booking.getBookingTime(), request.getNewSourceStation() + " to " + request.getNewDestinationStation());
        
        return buildModificationResponse(saved, "ROUTE_CHANGE", fareDifference, modificationCharge, totalAmount);
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

