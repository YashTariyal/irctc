package com.irctc.booking.service;

import com.irctc.booking.dto.HotelBookingRequest;
import com.irctc.booking.dto.HotelBookingResponse;
import com.irctc.booking.dto.HotelSearchRequest;
import com.irctc.booking.entity.Hotel;
import com.irctc.booking.entity.HotelBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.HotelBookingRepository;
import com.irctc.booking.repository.HotelRepository;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {
    
    @Mock
    private HotelRepository hotelRepository;
    
    @Mock
    private HotelBookingRepository hotelBookingRepository;
    
    @Mock
    private SimpleBookingRepository trainBookingRepository;
    
    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    
    @InjectMocks
    private HotelService hotelService;
    
    private Hotel hotel;
    private HotelBookingRequest bookingRequest;
    
    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");
        hotel.setLocation("Mumbai");
        hotel.setNearestStationCode("MMCT");
        hotel.setPricePerNight(BigDecimal.valueOf(2000));
        hotel.setAvailableRooms(10);
        hotel.setTotalRooms(20);
        hotel.setRating(BigDecimal.valueOf(4.5));
        hotel.setIsActive(true);
        
        bookingRequest = new HotelBookingRequest();
        bookingRequest.setUserId(123L);
        bookingRequest.setHotelId(1L);
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(1));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        bookingRequest.setNumberOfRooms(1);
        bookingRequest.setNumberOfGuests(2);
        bookingRequest.setGuestName("John Doe");
        bookingRequest.setGuestEmail("john@example.com");
        bookingRequest.setGuestPhone("1234567890");
    }
    
    @Test
    void testSearchHotels_ByLocation() {
        HotelSearchRequest request = new HotelSearchRequest();
        request.setLocation("Mumbai");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        
        when(hotelRepository.findAvailableHotelsByLocation("Mumbai"))
            .thenReturn(Arrays.asList(hotel));
        
        List<com.irctc.booking.dto.HotelSearchResponse> result = hotelService.searchHotels(request);
        
        assertEquals(1, result.size());
        assertEquals("Grand Hotel", result.get(0).getName());
        verify(hotelRepository, times(1)).findAvailableHotelsByLocation("Mumbai");
    }
    
    @Test
    void testSearchHotels_ByStationCode() {
        HotelSearchRequest request = new HotelSearchRequest();
        request.setStationCode("MMCT");
        request.setMinRating(BigDecimal.valueOf(4.0));
        
        when(hotelRepository.findRecommendedHotelsByStation("MMCT", BigDecimal.valueOf(4.0)))
            .thenReturn(Arrays.asList(hotel));
        
        List<com.irctc.booking.dto.HotelSearchResponse> result = hotelService.searchHotels(request);
        
        assertEquals(1, result.size());
        verify(hotelRepository, times(1)).findRecommendedHotelsByStation(eq("MMCT"), any());
    }
    
    @Test
    void testBookHotel_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelBookingRepository.findConflictingBookings(eq(1L), any(), any()))
            .thenReturn(new ArrayList<>());
        when(hotelBookingRepository.save(any(HotelBooking.class)))
            .thenAnswer(invocation -> {
                HotelBooking booking = invocation.getArgument(0);
                booking.setId(1L);
                if (booking.getBookingReference() == null) {
                    booking.setBookingReference("HTL" + System.currentTimeMillis());
                }
                return booking;
            });
        when(hotelRepository.save(any(Hotel.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        HotelBookingResponse response = hotelService.bookHotel(bookingRequest);
        
        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(1L, response.getHotelId());
        assertNotNull(response.getBookingReference());
        verify(hotelBookingRepository, times(1)).save(any(HotelBooking.class));
    }
    
    @Test
    void testBookHotel_HotelNotFound() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> {
            hotelService.bookHotel(bookingRequest);
        });
    }
    
    @Test
    void testBookHotel_NotEnoughRooms() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        HotelBooking conflicting = new HotelBooking();
        conflicting.setNumberOfRooms(10); // All rooms booked
        when(hotelBookingRepository.findConflictingBookings(eq(1L), any(), any()))
            .thenReturn(Arrays.asList(conflicting));
        
        assertThrows(IllegalStateException.class, () -> {
            hotelService.bookHotel(bookingRequest);
        });
    }
    
    @Test
    void testBookHotel_WithPackageDeal() {
        bookingRequest.setIsPackageDeal(true);
        bookingRequest.setTrainBookingId(456L);
        
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelBookingRepository.findConflictingBookings(eq(1L), any(), any()))
            .thenReturn(new ArrayList<>());
        when(hotelBookingRepository.save(any(HotelBooking.class)))
            .thenAnswer(invocation -> {
                HotelBooking booking = invocation.getArgument(0);
                booking.setId(1L);
                if (booking.getBookingReference() == null) {
                    booking.setBookingReference("HTL" + System.currentTimeMillis());
                }
                return booking;
            });
        when(hotelRepository.save(any(Hotel.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        HotelBookingResponse response = hotelService.bookHotel(bookingRequest);
        
        assertNotNull(response);
        assertTrue(response.getIsPackageDeal());
        assertNotNull(response.getDiscountAmount());
        assertTrue(response.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    void testGetHotelPackages() {
        when(hotelRepository.findRecommendedHotelsByStation(eq("MMCT"), any()))
            .thenReturn(Arrays.asList(hotel));
        
        com.irctc.booking.dto.HotelPackageResponse response = hotelService.getHotelPackages("NDLS-MMCT");
        
        assertNotNull(response);
        assertEquals("NDLS-MMCT", response.getRoute());
        assertNotNull(response.getPackages());
        assertFalse(response.getPackages().isEmpty());
    }
    
    @Test
    void testGetRecommendedHotels() {
        when(trainBookingRepository.findByUserId(123L))
            .thenReturn(new ArrayList<>());
        when(hotelRepository.findRecommendedHotelsByStation(anyString(), any()))
            .thenReturn(Arrays.asList(hotel));
        
        List<com.irctc.booking.dto.HotelSearchResponse> result = hotelService.getRecommendedHotels(123L);
        
        assertNotNull(result);
        verify(hotelRepository, atLeastOnce()).findRecommendedHotelsByStation(anyString(), any());
    }
}

