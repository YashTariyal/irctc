package com.irctc.booking.controller;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.service.SimpleBookingService;
import com.irctc.booking.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/bookings", "/api/bookings"}) // Support both versioned and non-versioned
public class SimpleBookingController {

    @Autowired
    private SimpleBookingService bookingService;

    @Autowired
    private IdempotencyService idempotencyService;

    @GetMapping
    public ResponseEntity<List<SimpleBooking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleBooking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pnr/{pnrNumber}")
    public ResponseEntity<SimpleBooking> getBookingByPnr(@PathVariable String pnrNumber) {
        return bookingService.getBookingByPnr(pnrNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SimpleBooking>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<SimpleBooking> createBooking(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                       @RequestBody SimpleBooking booking) {
        SimpleBooking newBooking = idempotencyService.process(
                idempotencyKey,
                "POST",
                "/api/bookings",
                booking,
                SimpleBooking.class,
                () -> bookingService.createBooking(booking)
        );
        return ResponseEntity.ok(newBooking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleBooking> updateBooking(@PathVariable Long id, @RequestBody SimpleBooking booking) {
        SimpleBooking updatedBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ADVANCED BOOKING APIs =====
    
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<SimpleBooking>> getUpcomingBookingsByUser(@PathVariable Long userId) {
        List<SimpleBooking> bookings = bookingService.getBookingsByUserId(userId).stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/user/{userId}/past")
    public ResponseEntity<List<SimpleBooking>> getPastBookingsByUser(@PathVariable Long userId) {
        List<SimpleBooking> bookings = bookingService.getBookingsByUserId(userId).stream()
                .filter(booking -> "CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/user/{userId}/confirmed")
    public ResponseEntity<List<SimpleBooking>> getConfirmedUpcomingBookingsByUser(@PathVariable Long userId) {
        List<SimpleBooking> bookings = bookingService.getBookingsByUserId(userId).stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/train/{trainId}")
    public ResponseEntity<List<SimpleBooking>> getBookingsByTrain(@PathVariable Long trainId) {
        List<SimpleBooking> bookings = bookingService.getAllBookings().stream()
                .filter(booking -> trainId.equals(booking.getTrainId()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/date/{journeyDate}")
    public ResponseEntity<List<SimpleBooking>> getBookingsByJourneyDate(@PathVariable String journeyDate) {
        List<SimpleBooking> bookings = bookingService.getAllBookings().stream()
                .filter(booking -> booking.getBookingTime().toString().contains(journeyDate))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SimpleBooking>> getBookingsByStatus(@PathVariable String status) {
        List<SimpleBooking> bookings = bookingService.getAllBookings().stream()
                .filter(booking -> status.equalsIgnoreCase(booking.getStatus()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<List<SimpleBooking>> getBookingsByPaymentStatus(@PathVariable String paymentStatus) {
        // For now, return all bookings (in real implementation, filter by payment status)
        List<SimpleBooking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/search/pnr")
    public ResponseEntity<List<SimpleBooking>> searchBookingsByPnr(@RequestParam String pnr) {
        List<SimpleBooking> bookings = bookingService.getAllBookings().stream()
                .filter(booking -> booking.getPnrNumber().toLowerCase().contains(pnr.toLowerCase()))
                .toList();
        return ResponseEntity.ok(bookings);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<SimpleBooking> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        SimpleBooking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setStatus(status);
        SimpleBooking updatedBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(updatedBooking);
    }
    
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<SimpleBooking> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        SimpleBooking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        // In real implementation, update payment status
        SimpleBooking updatedBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(updatedBooking);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SimpleBooking> cancelBookingById(@PathVariable Long id) {
        SimpleBooking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setStatus("CANCELLED");
        SimpleBooking updatedBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(updatedBooking);
    }
}
