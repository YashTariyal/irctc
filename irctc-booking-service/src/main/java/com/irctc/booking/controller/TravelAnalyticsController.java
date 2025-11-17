package com.irctc.booking.controller;

import com.irctc.booking.dto.analytics.TravelAnalyticsResponse;
import com.irctc.booking.dto.analytics.TravelExportResponse;
import com.irctc.booking.dto.analytics.TravelTimelineEntry;
import com.irctc.booking.service.TravelAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/user/{userId}")
public class TravelAnalyticsController {

    private final TravelAnalyticsService travelAnalyticsService;

    public TravelAnalyticsController(TravelAnalyticsService travelAnalyticsService) {
        this.travelAnalyticsService = travelAnalyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<TravelAnalyticsResponse> getTravelSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(travelAnalyticsService.getTravelAnalytics(userId));
    }

    @GetMapping("/favorite-routes")
    public ResponseEntity<List<TravelAnalyticsResponse.RouteSummary>> getFavoriteRoutes(@PathVariable Long userId) {
        return ResponseEntity.ok(travelAnalyticsService.getFavoriteRoutes(userId));
    }

    @GetMapping("/travel-timeline")
    public ResponseEntity<List<TravelTimelineEntry>> getTravelTimeline(@PathVariable Long userId) {
        return ResponseEntity.ok(travelAnalyticsService.getTimeline(userId));
    }

    @GetMapping("/export")
    public ResponseEntity<TravelExportResponse> exportTravelHistory(@PathVariable Long userId,
                                                                    @RequestParam(defaultValue = "csv") String format) {
        return ResponseEntity.ok(travelAnalyticsService.exportTravelHistory(userId, format));
    }
}

