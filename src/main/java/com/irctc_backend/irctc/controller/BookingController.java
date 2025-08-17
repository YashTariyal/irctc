package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.entity.Booking;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.service.BookingService;
import com.irctc_backend.irctc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@Tag(name = "Booking Management", description = "APIs for managing train bookings, PNR status, and booking operations")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    @Operation(summary = "Create a new booking", description = "Creates a new train booking and publishes notification events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = Booking.class))),
        @ApiResponse(responseCode = "400", description = "Invalid booking data"),
        @ApiResponse(responseCode = "404", description = "User, train, or passenger not found")
    })
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            Booking createdBooking = bookingService.createBooking(booking);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.findById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/pnr/{pnrNumber}")
    public ResponseEntity<?> getBookingByPnr(@PathVariable String pnrNumber) {
        Optional<Booking> booking = bookingService.findByPnrNumber(pnrNumber);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            List<Booking> bookings = bookingService.getBookingsByUser(user.get());
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<?> getUpcomingBookingsByUser(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            List<Booking> bookings = bookingService.getUpcomingBookingsByUser(user.get());
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/past")
    public ResponseEntity<?> getPastBookingsByUser(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            List<Booking> bookings = bookingService.getPastBookingsByUser(user.get());
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/confirmed")
    public ResponseEntity<?> getConfirmedUpcomingBookingsByUser(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            List<Booking> bookings = bookingService.getConfirmedUpcomingBookingsByUser(user.get());
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/train/{trainId}")
    public ResponseEntity<?> getBookingsByTrain(@PathVariable Long trainId) {
        // This would need a train service to get train by ID
        // For now, returning a placeholder response
        return ResponseEntity.ok("Bookings for train " + trainId);
    }
    
    @GetMapping("/date/{journeyDate}")
    public ResponseEntity<List<Booking>> getBookingsByJourneyDate(@PathVariable String journeyDate) {
        try {
            LocalDate date = LocalDate.parse(journeyDate);
            List<Booking> bookings = bookingService.getBookingsByJourneyDate(date);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable Booking.BookingStatus status) {
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<List<Booking>> getBookingsByPaymentStatus(@PathVariable Booking.PaymentStatus paymentStatus) {
        List<Booking> bookings = bookingService.getBookingsByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/search/pnr")
    public ResponseEntity<List<Booking>> searchBookingsByPnr(@RequestParam String pnr) {
        List<Booking> bookings = bookingService.searchBookingsByPnr(pnr);
        return ResponseEntity.ok(bookings);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam Booking.BookingStatus status) {
        try {
            Booking updatedBooking = bookingService.updateBookingStatus(id, status);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestParam Booking.PaymentStatus paymentStatus) {
        try {
            Booking updatedBooking = bookingService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            Booking cancelledBooking = bookingService.cancelBooking(id);
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
            return ResponseEntity.ok("Booking cancelled successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 