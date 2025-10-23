package com.irctc.booking.controller;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.service.SimpleBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class SimpleBookingController {

    @Autowired
    private SimpleBookingService bookingService;

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
    public ResponseEntity<SimpleBooking> createBooking(@RequestBody SimpleBooking booking) {
        SimpleBooking newBooking = bookingService.createBooking(booking);
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
}
