package com.irctc.payment.service;

import com.irctc.payment.dto.EmiPaymentResponse;
import com.irctc.payment.dto.PaymentPlanRequest;
import com.irctc.payment.dto.PaymentPlanResponse;
import com.irctc.payment.entity.EmiPayment;
import com.irctc.payment.entity.PaymentPlan;
import com.irctc.payment.repository.EmiPaymentRepository;
import com.irctc.payment.repository.PaymentPlanRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentPlanService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentPlanService.class);

    private final PaymentPlanRepository paymentPlanRepository;
    private final EmiPaymentRepository emiPaymentRepository;

    public PaymentPlanService(PaymentPlanRepository paymentPlanRepository,
                              EmiPaymentRepository emiPaymentRepository) {
        this.paymentPlanRepository = paymentPlanRepository;
        this.emiPaymentRepository = emiPaymentRepository;
    }

    @Transactional
    public PaymentPlanResponse createPlan(PaymentPlanRequest request) {
        PaymentPlan plan = new PaymentPlan();
        plan.setBookingId(request.getBookingId());
        plan.setUserId(request.getUserId());
        plan.setTotalAmount(request.getTotalAmount());
        plan.setDownPayment(request.getDownPayment());
        plan.setInstallments(request.getInstallments());
        plan.setInterestRate(request.getInterestRate());
        plan.setFrequency(Optional.ofNullable(request.getFrequency()).orElse("MONTHLY"));
        plan.setStatus("ACTIVE");
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(plan.getStartDate().plusMonths(plan.getInstallments()));
        if (TenantContext.hasTenant()) {
            plan.setTenantId(TenantContext.getTenantId());
        }

        BigDecimal emiAmount = calculateEmiAmount(plan);
        plan.setEmiAmount(emiAmount);
        PaymentPlan saved = paymentPlanRepository.save(plan);

        List<EmiPayment> schedule = generateSchedule(saved, emiAmount);
        emiPaymentRepository.saveAll(schedule);

        logger.info("✅ Payment plan {} created for booking {}", saved.getId(), saved.getBookingId());
        return toResponse(saved, schedule);
    }

    @Transactional(readOnly = true)
    public PaymentPlanResponse getPlan(Long planId) {
        PaymentPlan plan = paymentPlanRepository.findById(planId)
            .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("PaymentPlan", planId));
        List<EmiPayment> schedule = emiPaymentRepository.findByPaymentPlanId(planId);
        return toResponse(plan, schedule);
    }

    @Transactional(readOnly = true)
    public List<PaymentPlanResponse> getPlansByUser(Long userId) {
        return paymentPlanRepository.findByUserId(userId).stream()
            .map(plan -> toResponse(plan, emiPaymentRepository.findByPaymentPlanId(plan.getId())))
            .collect(Collectors.toList());
    }

    @Transactional
    public EmiPaymentResponse recordEmiPayment(Long emiId, BigDecimal amountPaid) {
        EmiPayment emiPayment = emiPaymentRepository.findById(emiId)
            .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("EmiPayment", emiId));

        emiPayment.setAmountPaid(amountPaid);
        emiPayment.setPaymentDate(LocalDate.now());
        emiPayment.setStatus("PAID");
        emiPaymentRepository.save(emiPayment);

        logger.info("💳 EMI installment {} paid for plan {}", emiPayment.getInstallmentNumber(), emiPayment.getPaymentPlanId());
        return toDto(emiPayment);
    }

    private BigDecimal calculateEmiAmount(PaymentPlan plan) {
        BigDecimal financedAmount = plan.getTotalAmount();
        if (plan.getDownPayment() != null) {
            financedAmount = financedAmount.subtract(plan.getDownPayment());
        }

        if (plan.getInstallments() == null || plan.getInstallments() == 0) {
            return financedAmount;
        }

        BigDecimal monthlyRate = Optional.ofNullable(plan.getInterestRate())
            .map(rate -> rate.divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP))
            .orElse(BigDecimal.ZERO);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return financedAmount.divide(BigDecimal.valueOf(plan.getInstallments()), 2, RoundingMode.HALF_UP);
        }

        double r = monthlyRate.doubleValue();
        double emi = (financedAmount.doubleValue() * r * Math.pow(1 + r, plan.getInstallments()))
            / (Math.pow(1 + r, plan.getInstallments()) - 1);

        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    private List<EmiPayment> generateSchedule(PaymentPlan plan, BigDecimal emiAmount) {
        return java.util.stream.IntStream.rangeClosed(1, plan.getInstallments())
            .mapToObj(i -> {
                EmiPayment emi = new EmiPayment();
                emi.setPaymentPlanId(plan.getId());
                emi.setInstallmentNumber(i);
                emi.setDueDate(plan.getStartDate().plusMonths(i - 1));
                emi.setAmountDue(emiAmount);
                emi.setStatus("DUE");
                emi.setTenantId(plan.getTenantId());
                return emi;
            })
            .collect(Collectors.toList());
    }

    private PaymentPlanResponse toResponse(PaymentPlan plan, List<EmiPayment> schedule) {
        PaymentPlanResponse response = new PaymentPlanResponse();
        response.setId(plan.getId());
        response.setBookingId(plan.getBookingId());
        response.setUserId(plan.getUserId());
        response.setTotalAmount(plan.getTotalAmount());
        response.setDownPayment(plan.getDownPayment());
        response.setEmiAmount(plan.getEmiAmount());
        response.setInterestRate(plan.getInterestRate());
        response.setInstallments(plan.getInstallments());
        response.setFrequency(plan.getFrequency());
        response.setStatus(plan.getStatus());
        response.setStartDate(plan.getStartDate());
        response.setEndDate(plan.getEndDate());
        response.setSchedule(schedule.stream()
            .map(this::toSummary)
            .collect(Collectors.toList()));
        return response;
    }

    private PaymentPlanResponse.EmiPaymentSummary toSummary(EmiPayment emi) {
        PaymentPlanResponse.EmiPaymentSummary summary = new PaymentPlanResponse.EmiPaymentSummary();
        summary.setId(emi.getId());
        summary.setInstallmentNumber(emi.getInstallmentNumber());
        summary.setDueDate(emi.getDueDate());
        summary.setAmountDue(emi.getAmountDue());
        summary.setAmountPaid(emi.getAmountPaid());
        summary.setStatus(emi.getStatus());
        return summary;
    }

    private EmiPaymentResponse toDto(EmiPayment emi) {
        EmiPaymentResponse dto = new EmiPaymentResponse();
        dto.setId(emi.getId());
        dto.setPaymentPlanId(emi.getPaymentPlanId());
        dto.setInstallmentNumber(emi.getInstallmentNumber());
        dto.setDueDate(emi.getDueDate());
        dto.setAmountDue(emi.getAmountDue());
        dto.setAmountPaid(emi.getAmountPaid());
        dto.setPaymentDate(emi.getPaymentDate());
        dto.setStatus(emi.getStatus());
        dto.setPenaltyAmount(Optional.ofNullable(emi.getPenaltyAmount()).orElse(BigDecimal.ZERO));
        dto.setPaymentReference(emi.getPaymentReference());
        return dto;
    }
}

