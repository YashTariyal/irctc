package com.irctc.payment.service;

import com.irctc.payment.dto.GiftCardBalanceResponse;
import com.irctc.payment.dto.GiftCardPurchaseRequest;
import com.irctc.payment.dto.GiftCardPurchaseResponse;
import com.irctc.payment.dto.GiftCardRedeemRequest;
import com.irctc.payment.entity.GiftCard;
import com.irctc.payment.entity.GiftCardTransaction;
import com.irctc.payment.exception.CustomException;
import com.irctc.payment.repository.GiftCardRepository;
import com.irctc.payment.repository.GiftCardTransactionRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GiftCardService {

    private static final Logger logger = LoggerFactory.getLogger(GiftCardService.class);

    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;

    public GiftCardService(GiftCardRepository giftCardRepository,
                           GiftCardTransactionRepository giftCardTransactionRepository) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
    }

    @Transactional
    public GiftCardPurchaseResponse purchaseGiftCard(GiftCardPurchaseRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new CustomException("Minimum gift card amount is 100");
        }

        GiftCard giftCard = new GiftCard();
        giftCard.setCode(generateCode());
        giftCard.setPurchaserUserId(request.getPurchaserUserId());
        giftCard.setRecipientEmail(request.getRecipientEmail());
        giftCard.setMessage(request.getMessage());
        giftCard.setInitialAmount(request.getAmount());
        giftCard.setBalanceAmount(request.getAmount());
        giftCard.setStatus("ACTIVE");
        giftCard.setExpiresAt(request.getExpiresAt() != null ? request.getExpiresAt() : LocalDateTime.now().plusMonths(12));
        if (TenantContext.hasTenant()) {
            giftCard.setTenantId(TenantContext.getTenantId());
        }

        GiftCard saved = giftCardRepository.save(giftCard);
        createTransaction(saved, "PURCHASE", request.getAmount());
        logger.info("🎁 Gift card {} purchased", saved.getCode());

        GiftCardPurchaseResponse response = new GiftCardPurchaseResponse();
        response.setCode(saved.getCode());
        response.setBalance(saved.getBalanceAmount());
        response.setStatus(saved.getStatus());
        response.setExpiresAt(saved.getExpiresAt());
        return response;
    }

    @Transactional
    public GiftCardBalanceResponse redeemGiftCard(GiftCardRedeemRequest request) {
        GiftCard giftCard = giftCardRepository.findByCode(request.getCode())
            .orElseThrow(() -> new CustomException("Invalid gift card code"));

        if (!"ACTIVE".equals(giftCard.getStatus()) || giftCard.getBalanceAmount().compareTo(request.getAmount()) < 0) {
            throw new CustomException("Gift card cannot cover the requested amount");
        }
        if (giftCard.getExpiresAt() != null && giftCard.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("Gift card expired");
        }

        giftCard.setBalanceAmount(giftCard.getBalanceAmount().subtract(request.getAmount()));
        if (giftCard.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
            giftCard.setStatus("CONSUMED");
        }
        giftCardRepository.save(giftCard);
        createTransaction(giftCard, "REDEEM", request.getAmount());

        logger.info("🎁 Gift card {} redeemed for {}", giftCard.getCode(), request.getAmount());
        return toResponse(giftCard);
    }

    @Transactional(readOnly = true)
    public GiftCardBalanceResponse getBalance(String code) {
        GiftCard giftCard = giftCardRepository.findByCode(code)
            .orElseThrow(() -> new CustomException("Gift card not found"));
        return toResponse(giftCard);
    }

    private void createTransaction(GiftCard giftCard, String type, BigDecimal amount) {
        GiftCardTransaction transaction = new GiftCardTransaction();
        transaction.setGiftCard(giftCard);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setReference(giftCard.getCode());
        if (TenantContext.hasTenant()) {
            transaction.setTenantId(TenantContext.getTenantId());
        }
        giftCardTransactionRepository.save(transaction);
    }

    private GiftCardBalanceResponse toResponse(GiftCard giftCard) {
        GiftCardBalanceResponse response = new GiftCardBalanceResponse();
        response.setCode(giftCard.getCode());
        response.setBalance(giftCard.getBalanceAmount());
        response.setStatus(giftCard.getStatus());
        response.setExpiresAt(giftCard.getExpiresAt());
        return response;
    }

    private String generateCode() {
        return "GC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

