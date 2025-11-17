package com.irctc.train.service;

import com.irctc.train.client.NotificationServiceClient;
import com.irctc.train.dto.PushNotificationRequest;
import com.irctc.train.entity.PriceAlert;
import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.repository.SimpleTrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertEvaluationServiceTest {

    @Mock
    private PriceAlertService priceAlertService;

    @Mock
    private SimpleTrainRepository trainRepository;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private PriceAlertEvaluationService evaluationService;

    private PriceAlert alert;
    private SimpleTrain train;

    @BeforeEach
    void setUp() {
        alert = new PriceAlert();
        alert.setId(1L);
        alert.setUserId(5L);
        alert.setAlertType("PRICE_DROP");
        alert.setTargetPrice(BigDecimal.valueOf(900));
        alert.setTrainNumber("12345");
        alert.setStatus("ACTIVE");

        train = new SimpleTrain();
        train.setTrainNumber("12345");
        train.setBaseFare(850.0);
        train.setAvailableSeats(50);
        train.setSourceStation("DEL");
        train.setDestinationStation("BCT");

        ReflectionTestUtils.setField(evaluationService, "notificationsEnabled", true);
    }

    @Test
    void shouldTriggerPriceAlert() {
        when(priceAlertService.getActiveAlerts()).thenReturn(List.of(alert));
        when(trainRepository.findByTrainNumber("12345")).thenReturn(Optional.of(train));
        when(priceAlertService.markTriggered(alert)).thenReturn(alert);
        when(notificationServiceClient.sendPushNotification(any())).thenReturn(ResponseEntity.accepted().build());

        evaluationService.evaluateAlerts();

        verify(priceAlertService).markTriggered(alert);
        ArgumentCaptor<PushNotificationRequest> captor = ArgumentCaptor.forClass(PushNotificationRequest.class);
        verify(notificationServiceClient).sendPushNotification(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(5L);
    }

    @Test
    void shouldSkipWhenNoTrainFound() {
        when(priceAlertService.getActiveAlerts()).thenReturn(List.of(alert));
        when(trainRepository.findByTrainNumber("12345")).thenReturn(Optional.empty());

        evaluationService.evaluateAlerts();

        verify(priceAlertService, never()).markTriggered(any());
        verify(notificationServiceClient, never()).sendPushNotification(any());
    }
}

