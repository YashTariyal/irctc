package com.irctc.booking.service;

import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.offline.OfflineSyncRequest;
import com.irctc.booking.dto.offline.OfflineSyncResponse;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.entity.SimplePassenger;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class OfflineSyncServiceTest {

    @Mock
    private SimpleBookingRepository bookingRepository;

    @Mock
    private TrainServiceClient trainServiceClient;

    @Mock
    private OfflineActionService offlineActionService;

    @InjectMocks
    private OfflineSyncService offlineSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateOfflineBundle() {
        SimpleBooking booking = new SimpleBooking();
        booking.setId(1L);
        booking.setUserId(99L);
        booking.setTrainId(101L);
        booking.setPnrNumber("PNR123");
        booking.setStatus("CONFIRMED");
        booking.setTotalFare(BigDecimal.valueOf(1234.50));
        booking.setBookingTime(LocalDateTime.now().minusDays(1));

        SimplePassenger passenger = new SimplePassenger();
        passenger.setId(11L);
        passenger.setName("John Doe");
        passenger.setAge(30);
        passenger.setGender("M");
        passenger.setSeatNumber("A1");
        passenger.setIdProofType("AADHAAR");
        booking.setPassengers(List.of(passenger));

        TrainServiceClient.TrainResponse trainResponse = new TrainServiceClient.TrainResponse();
        trainResponse.setId(101L);
        trainResponse.setTrainNumber("12001");
        trainResponse.setTrainName("Shatabdi Express");
        trainResponse.setSourceStation("NDLS");
        trainResponse.setDestinationStation("BCT");
        trainResponse.setTrainType("AC");
        trainResponse.setTrainClass("CC");
        trainResponse.setAvailableSeats(45);
        trainResponse.setBaseFare(1500.0);

        OfflineAction action = new OfflineAction();
        action.setId(5L);
        action.setUserId(99L);
        action.setActionType("CHECK_IN");
        action.setStatus("QUEUED");

        when(bookingRepository.findByUserId(99L)).thenReturn(List.of(booking));
        when(trainServiceClient.getTrainById(anyLong())).thenReturn(trainResponse);
        when(offlineActionService.getPendingActions(99L)).thenReturn(List.of(action));

        OfflineSyncRequest request = new OfflineSyncRequest();
        request.setUserId(99L);
        request.setTrainIds(List.of(101L));

        OfflineSyncResponse response = offlineSyncService.generateOfflineBundle(request);

        assertThat(response.getTickets()).hasSize(1);
        assertThat(response.getTrainSchedules()).hasSize(1);
        assertThat(response.getPendingActions()).hasSize(1);
        assertThat(response.getMetadata().getTicketCount()).isEqualTo(1);
        assertThat(response.getMetadata().getScheduleCount()).isEqualTo(1);
    }
}

