package com.irctc.booking.service;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleBookingService {

    @Autowired
    private SimpleBookingRepository bookingRepository;

    public List<SimpleBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<SimpleBooking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<SimpleBooking> getBookingByPnr(String pnrNumber) {
        return bookingRepository.findByPnrNumber(pnrNumber);
    }

    public List<SimpleBooking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public SimpleBooking createBooking(SimpleBooking booking) {
        booking.setPnrNumber(generatePnr());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public SimpleBooking updateBooking(Long id, SimpleBooking bookingDetails) {
        SimpleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        booking.setUserId(bookingDetails.getUserId());
        booking.setTrainId(bookingDetails.getTrainId());
        booking.setStatus(bookingDetails.getStatus());
        booking.setTotalFare(bookingDetails.getTotalFare());
        booking.setPassengers(bookingDetails.getPassengers());

        return bookingRepository.save(booking);
    }

    public void cancelBooking(Long id) {
        SimpleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    private String generatePnr() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
