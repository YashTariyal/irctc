package com.irctc.payment.service;

import com.irctc.payment.dto.OfflinePaymentRequest;
import com.irctc.payment.entity.OfflinePaymentIntent;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.OfflinePaymentIntentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OfflinePaymentQueueServiceTest {

    @Mock
    private OfflinePaymentIntentRepository repository;

    @Mock
    private SimplePaymentService simplePaymentService;

    @InjectMocks
    private OfflinePaymentQueueService queueService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldQueueIntent() {
        OfflinePaymentRequest request = new OfflinePaymentRequest();
        request.setUserId(1L);
        request.setBookingId(2L);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setCurrency("INR");
        request.setPaymentMethod("CARD");

        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setId(10L);
        when(repository.save(any())).thenReturn(intent);

        OfflinePaymentIntent saved = queueService.queue(request);
        assertThat(saved.getId()).isEqualTo(10L);

        ArgumentCaptor<OfflinePaymentIntent> captor = ArgumentCaptor.forClass(OfflinePaymentIntent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("QUEUED");
    }

    @Test
    void shouldProcessIntent() {
        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setId(5L);
        intent.setUserId(1L);
        intent.setBookingId(2L);
        intent.setAmount(BigDecimal.valueOf(500));
        intent.setCurrency("INR");
        intent.setPaymentMethod("CARD");
        intent.setStatus("QUEUED");

        when(repository.findById(5L)).thenReturn(Optional.of(intent));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SimplePayment payment = new SimplePayment();
        payment.setId(99L);
        payment.setStatus("COMPLETED");
        when(simplePaymentService.processPayment(any(SimplePayment.class))).thenReturn(payment);

        OfflinePaymentIntent processed = queueService.processIntent(5L);
        assertThat(processed.getStatus()).isEqualTo("COMPLETED");
        assertThat(processed.getProcessedPaymentId()).isEqualTo(99L);
    }
}

