package com.irctc.booking.service;

import com.irctc.booking.dto.*;
import com.irctc.booking.entity.Hotel;
import com.irctc.booking.entity.HotelBooking;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.HotelBookingRepository;
import com.irctc.booking.repository.HotelRepository;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for hotel booking operations
 */
@Service
public class HotelService {
    
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);
    
    private static final BigDecimal PACKAGE_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(10); // 10% discount for packages
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private HotelBookingRepository hotelBookingRepository;
    
    @Autowired
    private SimpleBookingRepository trainBookingRepository;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Search hotels by location and criteria
     */
    public List<HotelSearchResponse> searchHotels(HotelSearchRequest request) {
        logger.info("Searching hotels: location={}, checkIn={}, checkOut={}", 
            request.getLocation(), request.getCheckInDate(), request.getCheckOutDate());
        
        List<Hotel> hotels;
        
        // Search by location or station code
        if (request.getStationCode() != null && !request.getStationCode().isEmpty()) {
            hotels = hotelRepository.findRecommendedHotelsByStation(
                request.getStationCode(), 
                request.getMinRating() != null ? request.getMinRating() : BigDecimal.valueOf(3.0)
            );
        } else if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            if (request.getMinPrice() != null && request.getMaxPrice() != null) {
                hotels = hotelRepository.findHotelsByLocationAndPriceRange(
                    request.getLocation(),
                    request.getMinPrice(),
                    request.getMaxPrice()
                );
            } else {
                hotels = hotelRepository.findAvailableHotelsByLocation(request.getLocation());
            }
        } else {
            hotels = hotelRepository.findAll().stream()
                .filter(h -> h.getIsActive() && h.getAvailableRooms() > 0)
                .collect(Collectors.toList());
        }
        
        // Filter by amenities if provided
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            String[] requiredAmenities = request.getAmenities().split(",");
            hotels = hotels.stream()
                .filter(hotel -> {
                    if (hotel.getAmenities() == null) return false;
                    String hotelAmenities = hotel.getAmenities().toLowerCase();
                    for (String amenity : requiredAmenities) {
                        if (!hotelAmenities.contains(amenity.trim().toLowerCase())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        }
        
        // Filter by rating if provided
        if (request.getMinRating() != null) {
            hotels = hotels.stream()
                .filter(h -> h.getRating() != null && h.getRating().compareTo(request.getMinRating()) >= 0)
                .collect(Collectors.toList());
        }
        
        // Convert to response DTOs
        long nights = 1;
        if (request.getCheckInDate() != null && request.getCheckOutDate() != null) {
            nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        }
        final long finalNights = nights;
        
        return hotels.stream()
            .map(hotel -> {
                HotelSearchResponse response = new HotelSearchResponse();
                response.setId(hotel.getId());
                response.setName(hotel.getName());
                response.setLocation(hotel.getLocation());
                response.setNearestStationCode(hotel.getNearestStationCode());
                response.setAddress(hotel.getAddress());
                response.setCity(hotel.getCity());
                response.setState(hotel.getState());
                response.setRating(hotel.getRating());
                response.setPricePerNight(hotel.getPricePerNight());
                response.setAvailableRooms(hotel.getAvailableRooms());
                response.setAmenities(hotel.getAmenities());
                response.setDescription(hotel.getDescription());
                response.setImageUrl(hotel.getImageUrl());
                response.setNights((int) finalNights);
                
                // Calculate total price
                if (hotel.getPricePerNight() != null && finalNights > 0) {
                    BigDecimal totalPrice = hotel.getPricePerNight()
                        .multiply(BigDecimal.valueOf(finalNights))
                        .multiply(BigDecimal.valueOf(request.getNumberOfRooms() != null ? request.getNumberOfRooms() : 1));
                    response.setTotalPrice(totalPrice);
                }
                
                return response;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Book a hotel
     */
    @Transactional
    public HotelBookingResponse bookHotel(HotelBookingRequest request) {
        logger.info("Booking hotel: hotelId={}, userId={}, checkIn={}, checkOut={}", 
            request.getHotelId(), request.getUserId(), request.getCheckInDate(), request.getCheckOutDate());
        
        // Validate hotel exists
        Hotel hotel = hotelRepository.findById(request.getHotelId())
            .orElseThrow(() -> new EntityNotFoundException("Hotel", request.getHotelId()));
        
        if (!hotel.getIsActive()) {
            throw new IllegalStateException("Hotel is not active");
        }
        
        // Check availability
        List<HotelBooking> conflictingBookings = hotelBookingRepository.findConflictingBookings(
            request.getHotelId(),
            request.getCheckInDate(),
            request.getCheckOutDate()
        );
        
        int bookedRooms = conflictingBookings.stream()
            .mapToInt(HotelBooking::getNumberOfRooms)
            .sum();
        
        int availableRooms = hotel.getAvailableRooms() - bookedRooms;
        if (availableRooms < request.getNumberOfRooms()) {
            throw new IllegalStateException("Not enough rooms available. Available: " + availableRooms);
        }
        
        // Calculate total amount
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = hotel.getPricePerNight()
            .multiply(BigDecimal.valueOf(nights))
            .multiply(BigDecimal.valueOf(request.getNumberOfRooms()));
        
        // Apply package discount if applicable
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getIsPackageDeal() != null && request.getIsPackageDeal()) {
            discountAmount = totalAmount.multiply(PACKAGE_DISCOUNT_PERCENTAGE)
                .divide(BigDecimal.valueOf(100));
        } else if (request.getDiscountAmount() != null) {
            discountAmount = request.getDiscountAmount();
        }
        
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        
        // Create hotel booking
        HotelBooking booking = new HotelBooking();
        booking.setUserId(request.getUserId());
        booking.setHotelId(request.getHotelId());
        booking.setTrainBookingId(request.getTrainBookingId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfRooms(request.getNumberOfRooms());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("PENDING");
        booking.setIsPackageDeal(request.getIsPackageDeal() != null ? request.getIsPackageDeal() : false);
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setConfirmedAt(LocalDateTime.now());
        
        if (TenantContext.hasTenant()) {
            booking.setTenantId(TenantContext.getTenantId());
        }
        
        HotelBooking savedBooking = hotelBookingRepository.save(booking);
        
        // Update hotel availability
        hotel.setAvailableRooms(hotel.getAvailableRooms() - request.getNumberOfRooms());
        hotelRepository.save(hotel);
        
        // Publish booking event
        publishHotelBookingEvent(savedBooking);
        
        logger.info("✅ Hotel booking confirmed: bookingReference={}, hotelId={}", 
            savedBooking.getBookingReference(), request.getHotelId());
        
        return convertToResponse(savedBooking);
    }
    
    /**
     * Get hotel packages for a route (Train + Hotel combo)
     */
    public HotelPackageResponse getHotelPackages(String route) {
        logger.info("Getting hotel packages for route: {}", route);
        
        // Parse route (format: "ORIGIN-DESTINATION" or "ORIGIN_STATION-DESTINATION_STATION")
        String[] routeParts = route.split("-");
        if (routeParts.length != 2) {
            throw new IllegalArgumentException("Invalid route format. Expected: ORIGIN-DESTINATION");
        }
        
        String originStation = routeParts[0].trim();
        String destinationStation = routeParts[1].trim();
        
        // Find hotels near destination station
        List<Hotel> hotels = hotelRepository.findRecommendedHotelsByStation(
            destinationStation,
            BigDecimal.valueOf(3.0)
        );
        
        // Get train fare (simplified - in production, call train service)
        BigDecimal trainFare = BigDecimal.valueOf(500); // Default fare
        
        // Create package deals
        List<HotelPackageResponse.HotelPackage> packages = new ArrayList<>();
        for (Hotel hotel : hotels) {
            HotelPackageResponse.HotelPackage pkg = new HotelPackageResponse.HotelPackage();
            pkg.setHotelId(hotel.getId());
            pkg.setHotelName(hotel.getName());
            pkg.setLocation(hotel.getLocation());
            pkg.setHotelPricePerNight(hotel.getPricePerNight());
            pkg.setTrainFare(trainFare);
            
            // Calculate package price (1 night default)
            BigDecimal hotelTotal = hotel.getPricePerNight();
            BigDecimal packagePrice = trainFare.add(hotelTotal);
            
            // Apply 10% discount
            BigDecimal discount = packagePrice.multiply(PACKAGE_DISCOUNT_PERCENTAGE)
                .divide(BigDecimal.valueOf(100));
            BigDecimal finalPrice = packagePrice.subtract(discount);
            
            pkg.setPackagePrice(packagePrice);
            pkg.setDiscountAmount(discount);
            pkg.setFinalPrice(finalPrice);
            pkg.setSavings(discount);
            pkg.setNights(1);
            pkg.setDescription("Train + Hotel package with " + PACKAGE_DISCOUNT_PERCENTAGE + "% discount");
            
            packages.add(pkg);
        }
        
        HotelPackageResponse response = new HotelPackageResponse();
        response.setRoute(route);
        response.setOriginStation(originStation);
        response.setDestinationStation(destinationStation);
        response.setPackages(packages);
        
        return response;
    }
    
    /**
     * Get hotel recommendations based on user's booking history
     */
    public List<HotelSearchResponse> getRecommendedHotels(Long userId) {
        logger.info("Getting hotel recommendations for user: {}", userId);
        
        // Get user's recent train bookings
        List<SimpleBooking> recentBookings = trainBookingRepository.findByUserId(userId)
            .stream()
            .filter(b -> "CONFIRMED".equals(b.getStatus()))
            .limit(5)
            .collect(Collectors.toList());
        
        // Get user's recent hotel bookings
        List<HotelBooking> recentHotelBookings = hotelBookingRepository.findRecentBookingsByUser(userId)
            .stream()
            .limit(5)
            .collect(Collectors.toList());
        
        // Extract destination stations from train bookings
        List<String> destinationStations = new ArrayList<>();
        // In production, get actual destination from train service
        // For now, use a default list
        destinationStations.add("NDLS"); // New Delhi
        destinationStations.add("MMCT"); // Mumbai
        destinationStations.add("HWH"); // Howrah
        
        // Find recommended hotels near these stations
        List<Hotel> recommendedHotels = new ArrayList<>();
        for (String stationCode : destinationStations) {
            List<Hotel> hotels = hotelRepository.findRecommendedHotelsByStation(
                stationCode,
                BigDecimal.valueOf(4.0) // High rating hotels
            );
            recommendedHotels.addAll(hotels.stream().limit(3).collect(Collectors.toList()));
        }
        
        // Convert to response
        return recommendedHotels.stream()
            .map(hotel -> {
                HotelSearchResponse response = new HotelSearchResponse();
                response.setId(hotel.getId());
                response.setName(hotel.getName());
                response.setLocation(hotel.getLocation());
                response.setNearestStationCode(hotel.getNearestStationCode());
                response.setRating(hotel.getRating());
                response.setPricePerNight(hotel.getPricePerNight());
                response.setAvailableRooms(hotel.getAvailableRooms());
                response.setAmenities(hotel.getAmenities());
                response.setDescription(hotel.getDescription());
                response.setImageUrl(hotel.getImageUrl());
                return response;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Convert HotelBooking to response DTO
     */
    private HotelBookingResponse convertToResponse(HotelBooking booking) {
        HotelBookingResponse response = new HotelBookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setHotelId(booking.getHotelId());
        response.setTrainBookingId(booking.getTrainBookingId());
        response.setBookingReference(booking.getBookingReference());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setNumberOfRooms(booking.getNumberOfRooms());
        response.setNumberOfGuests(booking.getNumberOfGuests());
        response.setGuestName(booking.getGuestName());
        response.setGuestEmail(booking.getGuestEmail());
        response.setGuestPhone(booking.getGuestPhone());
        response.setTotalAmount(booking.getTotalAmount());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setFinalAmount(booking.getFinalAmount());
        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setIsPackageDeal(booking.getIsPackageDeal());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
    
    /**
     * Publish hotel booking event to Kafka
     */
    private void publishHotelBookingEvent(HotelBooking booking) {
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> event = new java.util.HashMap<>();
                event.put("bookingId", booking.getId());
                event.put("bookingReference", booking.getBookingReference());
                event.put("userId", booking.getUserId());
                event.put("hotelId", booking.getHotelId());
                event.put("trainBookingId", booking.getTrainBookingId());
                event.put("checkInDate", booking.getCheckInDate().toString());
                event.put("checkOutDate", booking.getCheckOutDate().toString());
                event.put("finalAmount", booking.getFinalAmount());
                event.put("status", booking.getStatus());
                event.put("isPackageDeal", booking.getIsPackageDeal());
                
                kafkaTemplate.send("hotel-booking-created", booking.getId().toString(), event);
                logger.info("Published hotel booking event: {}", booking.getBookingReference());
            } catch (Exception e) {
                logger.error("Error publishing hotel booking event: {}", e.getMessage(), e);
            }
        }
    }
}

