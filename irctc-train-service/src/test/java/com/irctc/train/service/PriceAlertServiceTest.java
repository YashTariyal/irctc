package com.irctc.train.service;

import com.irctc.train.dto.PriceAlertRequest;
import com.irctc.train.dto.PriceAlertUpdateRequest;
import com.irctc.train.entity.PriceAlert;
import com.irctc.train.exception.EntityNotFoundException;
import com.irctc.train.repository.PriceAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    private PriceAlert alert;

    @BeforeEach
    void setUp() {
        alert = new PriceAlert();
        alert.setId(1L);
        alert.setUserId(10L);
        alert.setAlertType("PRICE_DROP");
        alert.setTargetPrice(BigDecimal.valueOf(1000));
        alert.setStatus("ACTIVE");
    }

    @Test
    void shouldCreateAlert() {
        PriceAlertRequest request = new PriceAlertRequest();
        request.setUserId(10L);
        request.setAlertType("PRICE_DROP");
        request.setTargetPrice(BigDecimal.valueOf(900));
        request.setTrainNumber("12345");
        request.setTravelDate(LocalDate.now().plusDays(5));

        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(alert);

        var response = priceAlertService.createAlert(request);

        assertThat(response.getId()).isEqualTo(1L);
        verify(priceAlertRepository).save(any(PriceAlert.class));
    }

    @Test
    void shouldThrowWhenAlertMissing() {
        when(priceAlertRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> priceAlertService.getAlert(99L));
    }

    @Test
    void shouldUpdateAlert() {
        PriceAlertUpdateRequest request = new PriceAlertUpdateRequest();
        request.setTargetPrice(BigDecimal.valueOf(800));
        request.setStatus("CANCELLED");

        when(priceAlertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(priceAlertRepository.save(alert)).thenReturn(alert);

        var response = priceAlertService.updateAlert(1L, request);

        assertThat(response.getTargetPrice()).isEqualTo(BigDecimal.valueOf(800));
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldListAlertsForUser() {
        when(priceAlertRepository.findByUserId(10L)).thenReturn(List.of(alert));
        var responses = priceAlertService.getAlertsForUser(10L);
        assertThat(responses).hasSize(1);
    }

    @Test
    void shouldDeleteAlert() {
        when(priceAlertRepository.findById(1L)).thenReturn(Optional.of(alert));
        priceAlertService.deleteAlert(1L);
        verify(priceAlertRepository).delete(alert);
    }
}

