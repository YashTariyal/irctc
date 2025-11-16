package com.irctc.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.booking.dto.*;
import com.irctc.booking.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HotelControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private HotelService hotelService;
    
    @InjectMocks
    private HotelController hotelController;
    
    private ObjectMapper objectMapper;
    private HotelSearchResponse searchResponse;
    private HotelBookingResponse bookingResponse;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController)
                .setMessageConverters(converter)
                .build();
        
        searchResponse = new HotelSearchResponse();
        searchResponse.setId(1L);
        searchResponse.setName("Grand Hotel");
        searchResponse.setLocation("Mumbai");
        searchResponse.setRating(BigDecimal.valueOf(4.5));
        searchResponse.setPricePerNight(BigDecimal.valueOf(2000));
        
        bookingResponse = new HotelBookingResponse();
        bookingResponse.setId(1L);
        bookingResponse.setBookingReference("HTL123456");
        bookingResponse.setStatus("CONFIRMED");
        bookingResponse.setFinalAmount(BigDecimal.valueOf(4000));
    }
    
    @Test
    void testSearchHotels() throws Exception {
        when(hotelService.searchHotels(any(HotelSearchRequest.class)))
            .thenReturn(Arrays.asList(searchResponse));
        
        mockMvc.perform(get("/api/hotels/search")
                .param("location", "Mumbai")
                .param("checkIn", LocalDate.now().plusDays(1).toString())
                .param("checkOut", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.hotels").isArray());
    }
    
    @Test
    void testBookHotel() throws Exception {
        when(hotelService.bookHotel(any(HotelBookingRequest.class)))
            .thenReturn(bookingResponse);
        
        HotelBookingRequest request = new HotelBookingRequest();
        request.setUserId(123L);
        request.setHotelId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setNumberOfRooms(1);
        request.setGuestName("John Doe");
        
        mockMvc.perform(post("/api/hotels/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.bookingReference").value("HTL123456"));
    }
    
    @Test
    void testGetHotelPackages() throws Exception {
        HotelPackageResponse packageResponse = new HotelPackageResponse();
        packageResponse.setRoute("NDLS-MMCT");
        packageResponse.setOriginStation("NDLS");
        packageResponse.setDestinationStation("MMCT");
        
        when(hotelService.getHotelPackages("NDLS-MMCT"))
            .thenReturn(packageResponse);
        
        mockMvc.perform(get("/api/hotels/packages")
                .param("route", "NDLS-MMCT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("NDLS-MMCT"));
    }
    
    @Test
    void testGetRecommendedHotels() throws Exception {
        when(hotelService.getRecommendedHotels(123L))
            .thenReturn(Arrays.asList(searchResponse));
        
        mockMvc.perform(get("/api/hotels/recommendations")
                .param("userId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.recommendations").isArray());
    }
}

