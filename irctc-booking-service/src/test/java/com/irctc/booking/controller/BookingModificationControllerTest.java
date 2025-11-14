package com.irctc.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.dto.*;
import com.irctc.booking.service.BookingModificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimpleBookingController.class)
class BookingModificationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BookingModificationService modificationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetModificationOptions() throws Exception {
        // Given
        ModificationOptionsResponse response = new ModificationOptionsResponse();
        response.setBookingId(1L);
        response.setCurrentStatus("CONFIRMED");
        response.setCanModifyDate(true);
        response.setCanUpgradeSeat(true);
        response.setCanChangeRoute(true);
        response.setCanModifyPassengers(true);
        
        Map<String, BigDecimal> charges = new HashMap<>();
        charges.put("dateChange", new BigDecimal("200.00"));
        charges.put("seatUpgrade", new BigDecimal("100.00"));
        response.setModificationCharges(charges);
        
        when(modificationService.getModificationOptions(1L)).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/bookings/1/modification-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(1L))
                .andExpect(jsonPath("$.currentStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.canModifyDate").value(true))
                .andExpect(jsonPath("$.modificationCharges.dateChange").value(200.00));
    }
    
    @Test
    void testModifyDate() throws Exception {
        // Given
        DateChangeRequest request = new DateChangeRequest();
        request.setNewJourneyDate(LocalDateTime.now().plusDays(10));
        
        ModificationResponse response = new ModificationResponse();
        response.setBookingId(1L);
        response.setModificationType("DATE_CHANGE");
        response.setStatus("SUCCESS");
        response.setModificationCharge(new BigDecimal("200.00"));
        response.setTotalAmount(new BigDecimal("200.00"));
        
        when(modificationService.modifyDate(any(DateChangeRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/bookings/1/modify-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modificationType").value("DATE_CHANGE"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    void testUpgradeSeat() throws Exception {
        // Given
        SeatUpgradeRequest request = new SeatUpgradeRequest();
        request.setNewSeatClass("2AC");
        request.setNewFare(new BigDecimal("2500.00"));
        
        ModificationResponse response = new ModificationResponse();
        response.setBookingId(1L);
        response.setModificationType("SEAT_UPGRADE");
        response.setStatus("SUCCESS");
        response.setModificationCharge(new BigDecimal("100.00"));
        response.setTotalAmount(new BigDecimal("600.00"));
        
        when(modificationService.upgradeSeat(any(SeatUpgradeRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/bookings/1/upgrade-seat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modificationType").value("SEAT_UPGRADE"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    void testModifyPassengers() throws Exception {
        // Given
        PassengerModificationRequest request = new PassengerModificationRequest();
        request.setAdditionalFare(new BigDecimal("1200.00"));
        
        ModificationResponse response = new ModificationResponse();
        response.setBookingId(1L);
        response.setModificationType("PASSENGER_MODIFICATION");
        response.setStatus("SUCCESS");
        
        when(modificationService.modifyPassengers(any(PassengerModificationRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/bookings/1/modify-passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modificationType").value("PASSENGER_MODIFICATION"));
    }
    
    @Test
    void testChangeRoute() throws Exception {
        // Given
        RouteChangeRequest request = new RouteChangeRequest();
        request.setNewSourceStation("Mumbai");
        request.setNewDestinationStation("Delhi");
        request.setNewFare(new BigDecimal("3000.00"));
        
        ModificationResponse response = new ModificationResponse();
        response.setBookingId(1L);
        response.setModificationType("ROUTE_CHANGE");
        response.setStatus("SUCCESS");
        
        when(modificationService.changeRoute(any(RouteChangeRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/bookings/1/change-route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modificationType").value("ROUTE_CHANGE"));
    }
    
    @Test
    void testModifyDate_ValidationError() throws Exception {
        // Given
        DateChangeRequest request = new DateChangeRequest();
        // Missing newJourneyDate - should fail validation
        
        // When & Then
        mockMvc.perform(put("/api/bookings/1/modify-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

