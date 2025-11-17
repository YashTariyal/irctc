package com.irctc.payment.service;

import com.irctc.payment.dto.VoucherValidationResponse;
import com.irctc.payment.entity.Voucher;
import com.irctc.payment.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherService voucherService;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setCode("SAVE20");
        voucher.setVoucherType("PERCENTAGE");
        voucher.setDiscountValue(BigDecimal.valueOf(20));
        voucher.setStatus("ACTIVE");
        voucher.setMinOrderAmount(BigDecimal.valueOf(100));
        voucher.setExpiresAt(LocalDateTime.now().plusDays(10));
        voucher.setUsageCount(0);
        voucher.setUsageLimit(5);
    }

    @Test
    void shouldValidateVoucher() {
        when(voucherRepository.findByCode("SAVE20")).thenReturn(Optional.of(voucher));
        VoucherValidationResponse response = voucherService.validateVoucher("SAVE20", BigDecimal.valueOf(200));
        assertThat(response.isValid()).isTrue();
    }
}

