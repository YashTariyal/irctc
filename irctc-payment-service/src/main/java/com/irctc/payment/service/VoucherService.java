package com.irctc.payment.service;

import com.irctc.payment.dto.VoucherValidationResponse;
import com.irctc.payment.entity.Voucher;
import com.irctc.payment.exception.CustomException;
import com.irctc.payment.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public VoucherValidationResponse validateVoucher(String code, BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
            .orElseThrow(() -> new CustomException("Voucher not found"));

        boolean valid = isVoucherValid(voucher, orderAmount);
        VoucherValidationResponse response = new VoucherValidationResponse();
        response.setCode(code);
        response.setValid(valid);
        response.setExpiresAt(voucher.getExpiresAt());

        if (valid) {
            BigDecimal discount = calculateDiscount(voucher, orderAmount);
            response.setDiscountAmount(discount);
            response.setMessage("Voucher applied successfully");
        } else {
            response.setMessage("Voucher is not valid for this order");
        }
        return response;
    }

    private boolean isVoucherValid(Voucher voucher, BigDecimal orderAmount) {
        if (!"ACTIVE".equals(voucher.getStatus())) {
            return false;
        }
        if (voucher.getExpiresAt() != null && voucher.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            return false;
        }
        if (voucher.getMinOrderAmount() != null && orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            return false;
        }
        return true;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        BigDecimal discount = voucher.getDiscountValue();
        if ("PERCENTAGE".equalsIgnoreCase(voucher.getVoucherType())) {
            discount = orderAmount.multiply(discount).divide(BigDecimal.valueOf(100));
        }
        if (voucher.getMaxDiscountAmount() != null) {
            discount = discount.min(voucher.getMaxDiscountAmount());
        }
        return discount.min(orderAmount);
    }
}

