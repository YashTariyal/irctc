package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.BookingRepository;
import com.irctc_backend.irctc.repository.CoachRepository;
import com.irctc_backend.irctc.repository.SeatRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import com.irctc_backend.irctc.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @ExecutionTime("Create Train Booking")
    @CacheEvict(value = {"bookings", "train-schedules"}, allEntries = true)
    public Booking createBooking(Booking booking) {
        long startTime = System.currentTimeMillis();
        String requestId = LoggingUtil.generateRequestId();
        
        try {
            logger.info("Creating booking for train: {}, user: {}", 
                       booking.getTrain().getId(), booking.getUser().getId());
            
            // Validate train exists and is running
            Train train = trainRepository.findById(booking.getTrain().getId())
                .orElseThrow(() -> new RuntimeException("Train not found"));
            
            if (!train.getIsRunning()) {
                throw new RuntimeException("Train is not running");
            }
        
        // Generate PNR number
        booking.setPnrNumber(generatePNR());
        
        // Set booking date
        booking.setBookingDate(LocalDateTime.now());
        
        // Calculate fares
        calculateFares(booking);
        
        // Set default status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setIsCancelled(false);
        
        // Allocate seat if specified
        if (booking.getSeat() != null) {
            Seat seat = seatRepository.findById(booking.getSeat().getId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));
            
            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat is not available");
            }
            
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seatRepository.save(seat);
        }
        
        // Update coach availability
        if (booking.getCoach() != null) {
            Coach coach = coachRepository.findById(booking.getCoach().getId())
                .orElseThrow(() -> new RuntimeException("Coach not found"));
            
            coach.setAvailableSeats(coach.getAvailableSeats() - 1);
            coachRepository.save(coach);
        }
        
        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Publish booking confirmed event to Kafka for notifications
        try {
            notificationService.publishBookingConfirmedEvent(savedBooking);
            LoggingUtil.logKafkaEvent("BOOKING_CONFIRMED", "booking-confirmed", requestId, "SUCCESS");
        } catch (Exception e) {
            // Log the error but don't fail the booking
            LoggingUtil.logError("KAFKA_PUBLISH", "BOOKING", savedBooking.getId().toString(), 
                               "SYSTEM", "Failed to publish booking event to Kafka", e);
        }
        
        // Log successful booking creation
        LoggingUtil.logBusinessOperation("CREATE_BOOKING", "BOOKING", savedBooking.getId().toString(), 
                                       booking.getUser().getId().toString(), 
                                       "Booking created successfully with PNR: " + savedBooking.getPnrNumber());
        
        LoggingUtil.logDatabaseOperation("CREATE", "BOOKING", savedBooking.getId().toString(), 
                                       requestId, startTime);
        
        return savedBooking;
        } catch (Exception e) {
            LoggingUtil.logError("CREATE_BOOKING", "BOOKING", "N/A", 
                               booking.getUser().getId().toString(), "Failed to create booking", e);
            throw e;
        }
    }
    
    @ExecutionTime("Find Booking by PNR")
    @Cacheable(value = "bookings", key = "#pnrNumber")
    public Optional<Booking> findByPnrNumber(String pnrNumber) {
        return bookingRepository.findByPnrNumber(pnrNumber);
    }
    
    @Cacheable(value = "bookings", key = "#id")
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }
    
    @Cacheable(value = "bookings", key = "'all-bookings'")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    @Cacheable(value = "bookings", key = "'user-' + #user.id")
    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUser(user);
    }
    
    @Cacheable(value = "bookings", key = "'train-' + #train.id")
    public List<Booking> getBookingsByTrain(Train train) {
        return bookingRepository.findByTrain(train);
    }
    
    @Cacheable(value = "bookings", key = "'journey-date-' + #journeyDate")
    public List<Booking> getBookingsByJourneyDate(LocalDate journeyDate) {
        return bookingRepository.findByJourneyDate(journeyDate);
    }
    
    @Cacheable(value = "bookings", key = "'status-' + #status")
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    @Cacheable(value = "bookings", key = "'payment-status-' + #paymentStatus")
    public List<Booking> getBookingsByPaymentStatus(Booking.PaymentStatus paymentStatus) {
        return bookingRepository.findByPaymentStatus(paymentStatus);
    }
    
    @Cacheable(value = "bookings", key = "'upcoming-user-' + #user.id")
    public List<Booking> getUpcomingBookingsByUser(User user) {
        return bookingRepository.findUpcomingBookingsByUser(user, LocalDate.now());
    }
    
    @Cacheable(value = "bookings", key = "'past-user-' + #user.id")
    public List<Booking> getPastBookingsByUser(User user) {
        return bookingRepository.findPastBookingsByUser(user, LocalDate.now());
    }
    
    @Cacheable(value = "bookings", key = "'confirmed-upcoming-user-' + #user.id")
    public List<Booking> getConfirmedUpcomingBookingsByUser(User user) {
        return bookingRepository.findConfirmedUpcomingBookingsByUser(user, LocalDate.now());
    }
    
    @Cacheable(value = "bookings", key = "#pnr")
    public List<Booking> searchBookingsByPnr(String pnr) {
        return bookingRepository.findByPnrNumberContaining(pnr);
    }
    
    @ExecutionTime("Update Booking Status")
    @CacheEvict(value = {"bookings", "train-schedules"}, allEntries = true)
    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(status);
        
        // If cancelled, update seat and coach availability
        if (status == Booking.BookingStatus.CANCELLED) {
            booking.setIsCancelled(true);
            booking.setCancellationDate(LocalDateTime.now());
            
            // Free up seat
            if (booking.getSeat() != null) {
                Seat seat = seatRepository.findById(booking.getSeat().getId()).orElse(null);
                if (seat != null) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                }
            }
            
            // Update coach availability
            if (booking.getCoach() != null) {
                Coach coach = coachRepository.findById(booking.getCoach().getId()).orElse(null);
                if (coach != null) {
                    coach.setAvailableSeats(coach.getAvailableSeats() + 1);
                    coachRepository.save(coach);
                }
            }
        }
        
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Publish booking cancelled event to Kafka if cancelled
        if (status == Booking.BookingStatus.CANCELLED) {
            try {
                notificationService.publishBookingCancelledEvent(updatedBooking);
            } catch (Exception e) {
                System.err.println("Failed to publish booking cancelled event to Kafka: " + e.getMessage());
            }
        }
        
        return updatedBooking;
    }
    
    @CacheEvict(value = {"bookings", "train-schedules"}, allEntries = true)
    public Booking updatePaymentStatus(Long bookingId, Booking.PaymentStatus paymentStatus) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setPaymentStatus(paymentStatus);
        
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Publish payment completed event to Kafka if payment is completed
        if (paymentStatus == Booking.PaymentStatus.COMPLETED) {
            try {
                notificationService.publishPaymentCompletedEvent(updatedBooking);
            } catch (Exception e) {
                System.err.println("Failed to publish payment completed event to Kafka: " + e.getMessage());
            }
        }
        
        return updatedBooking;
    }
    
    @CacheEvict(value = {"bookings", "train-schedules"}, allEntries = true)
    public Booking cancelBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
    }
    
    @Cacheable(value = "bookings", key = "'count-train-' + #train.id + '-date-' + #journeyDate")
    public Long getConfirmedBookingsCountByTrainAndDate(Train train, LocalDate journeyDate) {
        return bookingRepository.countConfirmedBookingsByTrainAndDate(train, journeyDate);
    }
    
    private String generatePNR() {
        // Generate a 10-digit PNR number
        Random random = new Random();
        StringBuilder pnr = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            pnr.append(random.nextInt(10));
        }
        
        // Check if PNR already exists
        while (bookingRepository.existsByPnrNumber(pnr.toString())) {
            pnr = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                pnr.append(random.nextInt(10));
            }
        }
        
        return pnr.toString();
    }
    
    private void calculateFares(Booking booking) {
        Coach coach = coachRepository.findById(booking.getCoach().getId())
            .orElseThrow(() -> new RuntimeException("Coach not found"));
        
        // Base fare
        booking.setBaseFare(coach.getBaseFare());
        
        // Tatkal fare if applicable
        if (booking.getIsTatkal()) {
            booking.setTatkalFare(coach.getTatkalFare());
        } else {
            booking.setTatkalFare(BigDecimal.ZERO);
        }
        
        // Convenience fee (2% of base fare)
        BigDecimal convenienceFee = booking.getBaseFare().multiply(new BigDecimal("0.02"));
        booking.setConvenienceFee(convenienceFee);
        
        // GST (5% of base fare)
        BigDecimal gstAmount = booking.getBaseFare().multiply(new BigDecimal("0.05"));
        booking.setGstAmount(gstAmount);
        
        // Total fare
        BigDecimal totalFare = booking.getBaseFare()
            .add(booking.getTatkalFare())
            .add(booking.getConvenienceFee())
            .add(booking.getGstAmount());
        
        booking.setTotalFare(totalFare);
    }
} 