package com.irctc.booking.controller;

import com.irctc.booking.entity.CheckIn;
import com.irctc.booking.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for check-in operations
 */
@RestController
@RequestMapping("/api/bookings")
public class CheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * POST /api/bookings/{id}/check-in
     * Perform manual check-in for a booking
     */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<CheckIn> performCheckIn(@PathVariable Long id) {
        CheckIn checkIn = checkInService.performCheckIn(id, "MANUAL");
        return ResponseEntity.ok(checkIn);
    }
    
    /**
     * GET /api/bookings/{id}/check-in-status
     * Get check-in status for a booking
     */
    @GetMapping("/{id}/check-in-status")
    public ResponseEntity<CheckIn> getCheckInStatus(@PathVariable Long id) {
        CheckIn checkIn = checkInService.getCheckInStatus(id);
        return ResponseEntity.ok(checkIn);
    }
    
    /**
     * GET /api/bookings/user/{userId}/pending-checkins
     * Get pending check-ins for a user
     */
    @GetMapping("/user/{userId}/pending-checkins")
    public ResponseEntity<Map<String, Object>> getPendingCheckIns(@PathVariable Long userId) {
        List<CheckIn> pendingCheckIns = checkInService.getPendingCheckIns(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "pendingCheckIns", pendingCheckIns,
            "count", pendingCheckIns.size()
        ));
    }
}

