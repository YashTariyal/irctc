package com.irctc.payment.controller;

import com.irctc.payment.dto.*;
import com.irctc.payment.entity.WalletTransaction;
import com.irctc.payment.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wallet Controller
 * REST API for wallet operations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    
    @Autowired
    private WalletService walletService;
    
    /**
     * Get wallet balance
     * GET /api/wallet/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(@RequestParam String userId) {
        WalletResponse wallet = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(wallet);
    }
    
    /**
     * Top-up wallet
     * POST /api/wallet/top-up
     */
    @PostMapping("/top-up")
    public ResponseEntity<WalletTransactionResponse> topUp(
            @RequestParam String userId,
            @Valid @RequestBody WalletTopUpRequest request) {
        WalletTransactionResponse transaction = walletService.topUp(userId, request);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Make payment using wallet
     * POST /api/payments/wallet
     */
    @PostMapping("/payment")
    public ResponseEntity<WalletTransactionResponse> makePayment(
            @RequestParam String userId,
            @Valid @RequestBody WalletPaymentRequest request) {
        WalletTransactionResponse transaction = walletService.makePayment(userId, request);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Transfer money to another wallet
     * POST /api/wallet/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<WalletTransactionResponse> transfer(
            @RequestParam String userId,
            @Valid @RequestBody WalletTransferRequest request) {
        WalletTransactionResponse transaction = walletService.transfer(userId, request);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get transaction history
     * GET /api/wallet/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<WalletTransactionResponse> transactions = walletService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction history by type
     * GET /api/wallet/transactions?type=TOP_UP
     */
    @GetMapping("/transactions/type")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionsByType(
            @RequestParam String userId,
            @RequestParam WalletTransaction.TransactionType type) {
        List<WalletTransactionResponse> transactions = walletService.getTransactionHistoryByType(userId, type);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction by transaction ID
     * GET /api/wallet/transactions/{transactionId}
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<WalletTransactionResponse> getTransaction(@PathVariable String transactionId) {
        WalletTransactionResponse transaction = walletService.getTransaction(transactionId);
        return ResponseEntity.ok(transaction);
    }
}

