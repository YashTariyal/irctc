package com.irctc.booking.service;

import com.irctc.booking.entity.CheckIn;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.CheckInRepository;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling automated check-in functionality
 */
@Service
public class CheckInService {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);
    
    private static final int AUTO_CHECK_IN_HOURS_BEFORE_DEPARTURE = 4;
    
    @Autowired
    private CheckInRepository checkInRepository;
    
    @Autowired
    private SimpleBookingRepository bookingRepository;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Perform check-in for a booking
     */
    @Transactional
    public CheckIn performCheckIn(Long bookingId, String method) {
        logger.info("Performing check-in for booking: {}, method: {}", bookingId, method);
        
        SimpleBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        
        // Check if booking is confirmed
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalStateException("Only confirmed bookings can be checked in");
        }
        
        // Check if already checked in
        Optional<CheckIn> existingCheckIn = checkInRepository.findByBookingId(bookingId);
        if (existingCheckIn.isPresent() && "CHECKED_IN".equals(existingCheckIn.get().getStatus())) {
            logger.warn("Booking {} is already checked in", bookingId);
            return existingCheckIn.get();
        }
        
        CheckIn checkIn = existingCheckIn.orElse(new CheckIn());
        checkIn.setBookingId(bookingId);
        checkIn.setUserId(booking.getUserId());
        checkIn.setPnrNumber(booking.getPnrNumber());
        checkIn.setStatus("CHECKED_IN");
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setCheckInMethod(method);
        
        // Assign seat and coach (simplified - in production, use actual seat allocation logic)
        String seatNumber = assignSeat(booking);
        String coachNumber = assignCoach(booking);
        checkIn.setSeatNumber(seatNumber);
        checkIn.setCoachNumber(coachNumber);
        
        if (TenantContext.hasTenant()) {
            checkIn.setTenantId(TenantContext.getTenantId());
        }
        
        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        
        // Publish check-in event
        publishCheckInEvent(savedCheckIn);
        
        logger.info("✅ Check-in completed for booking: {}, seat: {}, coach: {}", 
            bookingId, seatNumber, coachNumber);
        
        return savedCheckIn;
    }
    
    /**
     * Get check-in status for a booking
     */
    public CheckIn getCheckInStatus(Long bookingId) {
        return checkInRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("CheckIn", bookingId));
    }
    
    /**
     * Get pending check-ins for a user
     */
    public List<CheckIn> getPendingCheckIns(Long userId) {
        return checkInRepository.findPendingCheckInsForUser(userId, LocalDateTime.now());
    }
    
    /**
     * Schedule automatic check-in for a booking
     * If departureTime is null, defaults to 24 hours from booking time
     */
    @Transactional
    public CheckIn scheduleAutoCheckIn(Long bookingId, LocalDateTime departureTime) {
        logger.info("Scheduling auto check-in for booking: {}, departure: {}", bookingId, departureTime);
        
        SimpleBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        
        // If departure time not provided, default to 24 hours from booking time
        if (departureTime == null) {
            departureTime = booking.getBookingTime() != null 
                ? booking.getBookingTime().plusHours(24)
                : LocalDateTime.now().plusHours(24);
        }
        
        LocalDateTime scheduledTime = departureTime.minusHours(AUTO_CHECK_IN_HOURS_BEFORE_DEPARTURE);
        
        // Don't schedule if scheduled time is in the past
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            logger.warn("Scheduled check-in time {} is in the past for booking: {}", scheduledTime, bookingId);
            scheduledTime = LocalDateTime.now().plusMinutes(5); // Schedule 5 minutes from now
        }
        
        CheckIn checkIn = checkInRepository.findByBookingId(bookingId)
            .orElse(new CheckIn());
        
        checkIn.setBookingId(bookingId);
        checkIn.setUserId(booking.getUserId());
        checkIn.setPnrNumber(booking.getPnrNumber());
        checkIn.setStatus("PENDING");
        checkIn.setScheduledCheckInTime(scheduledTime);
        checkIn.setDepartureTime(departureTime);
        checkIn.setCheckInMethod("AUTO");
        
        if (TenantContext.hasTenant()) {
            checkIn.setTenantId(TenantContext.getTenantId());
        }
        
        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        
        logger.info("✅ Auto check-in scheduled for booking: {} at {}", bookingId, scheduledTime);
        
        return savedCheckIn;
    }
    
    /**
     * Schedule automatic check-in for a booking (convenience method)
     */
    @Transactional
    public CheckIn scheduleAutoCheckIn(Long bookingId) {
        return scheduleAutoCheckIn(bookingId, null);
    }
    
    /**
     * Process scheduled check-ins (runs every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void processScheduledCheckIns() {
        LocalDateTime now = LocalDateTime.now();
        List<CheckIn> pendingCheckIns = checkInRepository
            .findByStatusAndScheduledCheckInTimeBefore("PENDING", now);
        
        if (pendingCheckIns.isEmpty()) {
            return;
        }
        
        logger.info("Processing {} scheduled check-ins", pendingCheckIns.size());
        
        for (CheckIn checkIn : pendingCheckIns) {
            try {
                performCheckIn(checkIn.getBookingId(), "AUTO");
                logger.info("✅ Auto check-in completed for booking: {}", checkIn.getBookingId());
            } catch (Exception e) {
                logger.error("Error processing auto check-in for booking {}: {}", 
                    checkIn.getBookingId(), e.getMessage(), e);
                checkIn.setStatus("FAILED");
                checkIn.setFailureReason(e.getMessage());
                checkInRepository.save(checkIn);
            }
        }
    }
    
    /**
     * Assign seat number (simplified - in production, use actual seat allocation)
     */
    private String assignSeat(SimpleBooking booking) {
        // Simplified seat assignment - in production, use actual seat allocation logic
        Random random = new Random();
        int seatNumber = random.nextInt(72) + 1; // 1-72 seats per coach
        return String.valueOf(seatNumber);
    }
    
    /**
     * Assign coach number (simplified - in production, use actual coach allocation)
     */
    private String assignCoach(SimpleBooking booking) {
        // Simplified coach assignment - in production, use actual coach allocation logic
        String[] coaches = {"A1", "A2", "A3", "B1", "B2", "B3", "S1", "S2", "S3"};
        Random random = new Random();
        return coaches[random.nextInt(coaches.length)];
    }
    
    /**
     * Publish check-in event to Kafka
     */
    private void publishCheckInEvent(CheckIn checkIn) {
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("bookingId", checkIn.getBookingId());
                event.put("userId", checkIn.getUserId());
                event.put("pnrNumber", checkIn.getPnrNumber());
                event.put("seatNumber", checkIn.getSeatNumber());
                event.put("coachNumber", checkIn.getCoachNumber());
                event.put("checkInTime", checkIn.getCheckInTime());
                event.put("checkInMethod", checkIn.getCheckInMethod());
                
                kafkaTemplate.send("check-in-completed", checkIn.getBookingId().toString(), event);
                logger.info("Published check-in event for booking: {}", checkIn.getBookingId());
            } catch (Exception e) {
                logger.error("Error publishing check-in event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Send check-in reminder notification
     */
    @Async
    public CompletableFuture<Void> sendCheckInReminder(Long bookingId) {
        try {
            SimpleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
            
            // In production, send notification via notification service
            logger.info("Sending check-in reminder for booking: {}, user: {}", 
                bookingId, booking.getUserId());
            
            // Publish reminder event
            if (kafkaTemplate != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("bookingId", bookingId);
                event.put("userId", booking.getUserId());
                event.put("pnrNumber", booking.getPnrNumber());
                event.put("type", "CHECK_IN_REMINDER");
                
                kafkaTemplate.send("check-in-reminder", bookingId.toString(), event);
            }
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Error sending check-in reminder: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

