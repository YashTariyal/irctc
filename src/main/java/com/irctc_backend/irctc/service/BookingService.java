package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.BookingRepository;
import com.irctc_backend.irctc.repository.CoachRepository;
import com.irctc_backend.irctc.repository.SeatRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BookingService {
    
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
    
    public Booking createBooking(Booking booking) {
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
        } catch (Exception e) {
            // Log the error but don't fail the booking
            System.err.println("Failed to publish booking event to Kafka: " + e.getMessage());
        }
        
        return savedBooking;
    }
    
    public Optional<Booking> findByPnrNumber(String pnrNumber) {
        return bookingRepository.findByPnrNumber(pnrNumber);
    }
    
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUser(user);
    }
    
    public List<Booking> getBookingsByTrain(Train train) {
        return bookingRepository.findByTrain(train);
    }
    
    public List<Booking> getBookingsByJourneyDate(LocalDate journeyDate) {
        return bookingRepository.findByJourneyDate(journeyDate);
    }
    
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    public List<Booking> getBookingsByPaymentStatus(Booking.PaymentStatus paymentStatus) {
        return bookingRepository.findByPaymentStatus(paymentStatus);
    }
    
    public List<Booking> getUpcomingBookingsByUser(User user) {
        return bookingRepository.findUpcomingBookingsByUser(user, LocalDate.now());
    }
    
    public List<Booking> getPastBookingsByUser(User user) {
        return bookingRepository.findPastBookingsByUser(user, LocalDate.now());
    }
    
    public List<Booking> getConfirmedUpcomingBookingsByUser(User user) {
        return bookingRepository.findConfirmedUpcomingBookingsByUser(user, LocalDate.now());
    }
    
    public List<Booking> searchBookingsByPnr(String pnr) {
        return bookingRepository.findByPnrNumberContaining(pnr);
    }
    
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
    
    public Booking cancelBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
    }
    
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