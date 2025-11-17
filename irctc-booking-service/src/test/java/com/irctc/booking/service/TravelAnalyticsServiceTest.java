package com.irctc.booking.service;

import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.analytics.TravelAnalyticsResponse;
import com.irctc.booking.dto.analytics.TravelExportResponse;
import com.irctc.booking.dto.analytics.TravelTimelineEntry;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelAnalyticsServiceTest {

    @Mock
    private SimpleBookingRepository bookingRepository;

    @Mock
    private TrainServiceClient trainServiceClient;

    @InjectMocks
    private TravelAnalyticsService travelAnalyticsService;

    private SimpleBooking booking1;
    private SimpleBooking booking2;

    @BeforeEach
    void setUp() {
        booking1 = new SimpleBooking();
        booking1.setId(1L);
        booking1.setUserId(100L);
        booking1.setTrainId(10L);
        booking1.setPnrNumber("PNR100");
        booking1.setStatus("CONFIRMED");
        booking1.setBookingTime(LocalDateTime.of(2025, 1, 15, 10, 0));
        booking1.setTotalFare(BigDecimal.valueOf(1200));

        booking2 = new SimpleBooking();
        booking2.setId(2L);
        booking2.setUserId(100L);
        booking2.setTrainId(11L);
        booking2.setPnrNumber("PNR101");
        booking2.setStatus("CANCELLED");
        booking2.setBookingTime(LocalDateTime.of(2025, 2, 10, 12, 30));
        booking2.setTotalFare(BigDecimal.valueOf(800));

        TrainServiceClient.TrainResponse trainA = new TrainServiceClient.TrainResponse();
        trainA.setId(10L);
        trainA.setTrainName("Express A");
        trainA.setSourceStation("NDLS");
        trainA.setDestinationStation("BCT");
        trainA.setDistance(1384);

        TrainServiceClient.TrainResponse trainB = new TrainServiceClient.TrainResponse();
        trainB.setId(11L);
        trainB.setTrainName("Express B");
        trainB.setSourceStation("NDLS");
        trainB.setDestinationStation("HWH");
        trainB.setDistance(1450);

        when(bookingRepository.findByUserId(100L)).thenReturn(List.of(booking1, booking2));
        when(trainServiceClient.getTrainById(10L)).thenReturn(trainA);
        when(trainServiceClient.getTrainById(11L)).thenReturn(trainB);
    }

    @Test
    void shouldComputeTravelAnalytics() {
        TravelAnalyticsResponse response = travelAnalyticsService.getTravelAnalytics(100L);

        assertThat(response.getTotalBookings()).isEqualTo(2);
        assertThat(response.getConfirmedTrips()).isEqualTo(1);
        assertThat(response.getCancelledTrips()).isEqualTo(1);
        assertThat(response.getTotalSpent()).isEqualByComparingTo("2000");
        assertThat(response.getFavoriteRoutes()).hasSize(2);
        assertThat(response.getTopTrains()).hasSize(2);
    }

    @Test
    void shouldReturnTimeline() {
        List<TravelTimelineEntry> timeline = travelAnalyticsService.getTimeline(100L);
        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).getPnrNumber()).isEqualTo("PNR101"); // most recent first
    }

    @Test
    void shouldExportHistoryAsCsv() {
        TravelExportResponse export = travelAnalyticsService.exportTravelHistory(100L, "csv");
        assertThat(export.getFormat()).isEqualTo("csv");
        assertThat(export.getData()).isNotBlank();
        String csv = new String(Base64.getDecoder().decode(export.getData()));
        assertThat(csv).contains("PNR100").contains("PNR101");
    }
}

