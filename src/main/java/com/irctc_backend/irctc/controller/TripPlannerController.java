package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.TripPlanRequest;
import com.irctc_backend.irctc.dto.TripPlanResponse;
import com.irctc_backend.irctc.service.TripPlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-planner")
@Tag(name = "Trip Planner", description = "Multi-city trip planning with connections")
public class TripPlannerController {

    @Autowired
    private TripPlannerService tripPlannerService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trip Planner Service is up");
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Search itineraries with up to N connections")
    public ResponseEntity<List<TripPlanResponse>> search(@Valid @RequestBody TripPlanRequest request) {
        List<TripPlanResponse> itineraries = tripPlannerService.findItineraries(request);
        return ResponseEntity.ok(itineraries);
    }
}


