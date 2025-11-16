package com.irctc.booking.controller;

import com.irctc.booking.dto.*;
import com.irctc.booking.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for hotel booking operations
 */
@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    
    @Autowired
    private HotelService hotelService;
    
    /**
     * GET /api/hotels/search
     * Search hotels by location, dates, and criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchHotels(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String stationCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "1") Integer numberOfRooms,
            @RequestParam(required = false, defaultValue = "1") Integer numberOfGuests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) String amenities) {
        
        HotelSearchRequest request = new HotelSearchRequest();
        request.setLocation(location);
        request.setStationCode(stationCode);
        request.setCheckInDate(checkIn);
        request.setCheckOutDate(checkOut);
        request.setNumberOfRooms(numberOfRooms);
        request.setNumberOfGuests(numberOfGuests);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(minRating);
        request.setAmenities(amenities);
        
        List<HotelSearchResponse> hotels = hotelService.searchHotels(request);
        
        return ResponseEntity.ok(Map.of(
            "hotels", hotels,
            "count", hotels.size()
        ));
    }
    
    /**
     * POST /api/hotels/book
     * Book a hotel
     */
    @PostMapping("/book")
    public ResponseEntity<HotelBookingResponse> bookHotel(@RequestBody HotelBookingRequest request) {
        HotelBookingResponse response = hotelService.bookHotel(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/hotels/packages
     * Get hotel packages for a route (Train + Hotel combo)
     */
    @GetMapping("/packages")
    public ResponseEntity<HotelPackageResponse> getHotelPackages(@RequestParam String route) {
        HotelPackageResponse response = hotelService.getHotelPackages(route);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/hotels/recommendations
     * Get hotel recommendations based on user's booking history
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendedHotels(@RequestParam Long userId) {
        List<HotelSearchResponse> recommendations = hotelService.getRecommendedHotels(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "recommendations", recommendations,
            "count", recommendations.size()
        ));
    }
}

