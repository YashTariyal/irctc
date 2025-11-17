package com.irctc.payment.service;

import com.irctc.payment.dto.GiftCardPurchaseRequest;
import com.irctc.payment.dto.GiftCardRedeemRequest;
import com.irctc.payment.entity.GiftCard;
import com.irctc.payment.repository.GiftCardRepository;
import com.irctc.payment.repository.GiftCardTransactionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftCardServiceTest {

    @Mock
    private GiftCardRepository giftCardRepository;

    @Mock
    private GiftCardTransactionRepository giftCardTransactionRepository;

    @InjectMocks
    private GiftCardService giftCardService;

    private GiftCard giftCard;

    @BeforeEach
    void setUp() {
        giftCard = new GiftCard();
        giftCard.setId(1L);
        giftCard.setCode("GC-12345678");
        giftCard.setInitialAmount(BigDecimal.valueOf(500));
        giftCard.setBalanceAmount(BigDecimal.valueOf(500));
        giftCard.setStatus("ACTIVE");
        giftCard.setExpiresAt(LocalDateTime.now().plusDays(30));
    }

    @Test
    void shouldPurchaseGiftCard() {
        when(giftCardRepository.save(any(GiftCard.class))).thenReturn(giftCard);
        when(giftCardTransactionRepository.save(any())).thenReturn(null);

        GiftCardPurchaseRequest request = new GiftCardPurchaseRequest();
        request.setAmount(BigDecimal.valueOf(500));

        var response = giftCardService.purchaseGiftCard(request);
        assertThat(response.getCode()).isEqualTo("GC-12345678");
    }

    @Test
    void shouldRedeemGiftCard() {
        when(giftCardRepository.findByCode("GC-12345678")).thenReturn(Optional.of(giftCard));
        when(giftCardRepository.save(any(GiftCard.class))).thenReturn(giftCard);
        when(giftCardTransactionRepository.save(any())).thenReturn(null);

        GiftCardRedeemRequest request = new GiftCardRedeemRequest();
        request.setCode("GC-12345678");
        request.setAmount(BigDecimal.valueOf(200));

        var response = giftCardService.redeemGiftCard(request);
        assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(300));
    }
}

