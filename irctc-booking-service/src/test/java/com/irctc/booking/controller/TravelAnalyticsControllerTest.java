package com.irctc.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.dto.analytics.TravelAnalyticsResponse;
import com.irctc.booking.dto.analytics.TravelExportResponse;
import com.irctc.booking.dto.analytics.TravelTimelineEntry;
import com.irctc.booking.service.TravelAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TravelAnalyticsControllerTest {

    private MockMvc mockMvc;
    private TravelAnalyticsService travelAnalyticsService;

    @BeforeEach
    void setUp() {
        travelAnalyticsService = Mockito.mock(TravelAnalyticsService.class);
        TravelAnalyticsController controller = new TravelAnalyticsController(travelAnalyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnAnalyticsSummary() throws Exception {
        TravelAnalyticsResponse response = new TravelAnalyticsResponse();
        response.setUserId(1L);
        response.setTotalBookings(3);
        response.setTotalSpent(BigDecimal.valueOf(2500));
        when(travelAnalyticsService.getTravelAnalytics(1L)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/user/1/analytics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalBookings").value(3));
    }

    @Test
    void shouldReturnFavoriteRoutes() throws Exception {
        TravelAnalyticsResponse.RouteSummary summary = new TravelAnalyticsResponse.RouteSummary();
        summary.setSourceStation("NDLS");
        summary.setDestinationStation("BCT");
        summary.setTrips(4L);
        when(travelAnalyticsService.getFavoriteRoutes(anyLong())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/bookings/user/1/favorite-routes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].sourceStation").value("NDLS"));
    }

    @Test
    void shouldReturnTimeline() throws Exception {
        TravelTimelineEntry entry = new TravelTimelineEntry();
        entry.setPnrNumber("PNR1");
        when(travelAnalyticsService.getTimeline(anyLong())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/bookings/user/1/travel-timeline"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pnrNumber").value("PNR1"));
    }

    @Test
    void shouldExportHistory() throws Exception {
        TravelExportResponse export = new TravelExportResponse();
        export.setFormat("csv");
        export.setData(Base64.getEncoder().encodeToString("test".getBytes()));
        when(travelAnalyticsService.exportTravelHistory(1L, "csv")).thenReturn(export);

        mockMvc.perform(get("/api/bookings/user/1/export").param("format", "csv")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.format").value("csv"));
    }
}

