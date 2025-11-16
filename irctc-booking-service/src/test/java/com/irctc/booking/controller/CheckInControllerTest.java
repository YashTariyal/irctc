package com.irctc.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.CheckIn;
import com.irctc.booking.service.CheckInService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CheckInControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private CheckInService checkInService;
    
    @InjectMocks
    private CheckInController checkInController;
    
    private ObjectMapper objectMapper;
    private CheckIn checkIn;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(checkInController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        
        checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setBookingId(1L);
        checkIn.setUserId(123L);
        checkIn.setPnrNumber("PNR123456");
        checkIn.setStatus("CHECKED_IN");
        checkIn.setSeatNumber("12");
        checkIn.setCoachNumber("A1");
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setCheckInMethod("MANUAL");
    }
    
    @Test
    void testPerformCheckIn() throws Exception {
        when(checkInService.performCheckIn(1L, "MANUAL")).thenReturn(checkIn);
        
        mockMvc.perform(post("/api/bookings/1/check-in"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.seatNumber").value("12"))
                .andExpect(jsonPath("$.coachNumber").value("A1"));
    }
    
    @Test
    void testGetCheckInStatus() throws Exception {
        when(checkInService.getCheckInStatus(1L)).thenReturn(checkIn);
        
        mockMvc.perform(get("/api/bookings/1/check-in-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.bookingId").value(1));
    }
    
    @Test
    void testGetPendingCheckIns() throws Exception {
        List<CheckIn> pendingCheckIns = Arrays.asList(checkIn);
        when(checkInService.getPendingCheckIns(123L)).thenReturn(pendingCheckIns);
        
        mockMvc.perform(get("/api/bookings/user/123/pending-checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.pendingCheckIns").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }
}

