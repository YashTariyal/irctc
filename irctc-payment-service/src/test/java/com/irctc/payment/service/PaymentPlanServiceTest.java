package com.irctc.payment.service;

import com.irctc.payment.dto.PaymentPlanRequest;
import com.irctc.payment.dto.PaymentPlanResponse;
import com.irctc.payment.entity.EmiPayment;
import com.irctc.payment.entity.PaymentPlan;
import com.irctc.payment.repository.EmiPaymentRepository;
import com.irctc.payment.repository.PaymentPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPlanServiceTest {

    @Mock
    private PaymentPlanRepository paymentPlanRepository;

    @Mock
    private EmiPaymentRepository emiPaymentRepository;

    @InjectMocks
    private PaymentPlanService paymentPlanService;

    private PaymentPlan plan;

    @BeforeEach
    void setUp() {
        plan = new PaymentPlan();
        plan.setId(10L);
        plan.setBookingId(1L);
        plan.setUserId(2L);
        plan.setTotalAmount(BigDecimal.valueOf(1200));
        plan.setInstallments(12);
        plan.setInterestRate(BigDecimal.valueOf(10));
        plan.setStatus("ACTIVE");
        plan.setStartDate(java.time.LocalDate.now());
    }

    @Test
    void shouldCreatePlanAndSchedule() {
        PaymentPlanRequest request = new PaymentPlanRequest();
        request.setBookingId(1L);
        request.setUserId(2L);
        request.setTotalAmount(BigDecimal.valueOf(1200));
        request.setInstallments(12);
        request.setInterestRate(BigDecimal.TEN);

        when(paymentPlanRepository.save(any(PaymentPlan.class))).thenReturn(plan);

        PaymentPlanResponse response = paymentPlanService.createPlan(request);

        assertThat(response.getId()).isEqualTo(10L);
        verify(emiPaymentRepository).saveAll(anyList());
    }

    @Test
    void shouldReturnPlanById() {
        when(paymentPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(emiPaymentRepository.findByPaymentPlanId(10L)).thenReturn(List.of());

        PaymentPlanResponse response = paymentPlanService.getPlan(10L);

        assertThat(response.getBookingId()).isEqualTo(1L);
    }

    @Test
    void shouldRecordEmiPayment() {
        EmiPayment emiPayment = new EmiPayment();
        emiPayment.setId(5L);
        emiPayment.setPaymentPlanId(10L);
        emiPayment.setInstallmentNumber(1);
        emiPayment.setAmountDue(BigDecimal.valueOf(100));
        emiPayment.setStatus("DUE");

        when(emiPaymentRepository.findById(5L)).thenReturn(Optional.of(emiPayment));

        paymentPlanService.recordEmiPayment(5L, BigDecimal.valueOf(100));

        ArgumentCaptor<EmiPayment> captor = ArgumentCaptor.forClass(EmiPayment.class);
        verify(emiPaymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PAID");
    }
}

