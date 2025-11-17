package com.irctc.payment.controller;

import com.irctc.payment.dto.GiftCardBalanceResponse;
import com.irctc.payment.dto.GiftCardPurchaseRequest;
import com.irctc.payment.dto.GiftCardPurchaseResponse;
import com.irctc.payment.dto.GiftCardRedeemRequest;
import com.irctc.payment.dto.VoucherValidationResponse;
import com.irctc.payment.service.GiftCardService;
import com.irctc.payment.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping
public class GiftCardController {

    private final GiftCardService giftCardService;
    private final VoucherService voucherService;

    public GiftCardController(GiftCardService giftCardService, VoucherService voucherService) {
        this.giftCardService = giftCardService;
        this.voucherService = voucherService;
    }

    @PostMapping("/api/gift-cards/purchase")
    public ResponseEntity<GiftCardPurchaseResponse> purchase(@RequestBody GiftCardPurchaseRequest request) {
        return ResponseEntity.ok(giftCardService.purchaseGiftCard(request));
    }

    @PostMapping("/api/gift-cards/redeem")
    public ResponseEntity<GiftCardBalanceResponse> redeem(@RequestBody GiftCardRedeemRequest request) {
        return ResponseEntity.ok(giftCardService.redeemGiftCard(request));
    }

    @GetMapping("/api/gift-cards/{code}/balance")
    public ResponseEntity<GiftCardBalanceResponse> getBalance(@PathVariable String code) {
        return ResponseEntity.ok(giftCardService.getBalance(code));
    }

    @GetMapping("/api/vouchers/validate")
    public ResponseEntity<VoucherValidationResponse> validateVoucher(@RequestParam String code,
                                                                     @RequestParam BigDecimal orderAmount) {
        return ResponseEntity.ok(voucherService.validateVoucher(code, orderAmount));
    }
}

