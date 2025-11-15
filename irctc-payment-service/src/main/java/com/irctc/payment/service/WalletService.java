package com.irctc.payment.service;

import com.irctc.payment.dto.*;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.entity.Wallet;
import com.irctc.payment.entity.WalletTransaction;
import com.irctc.payment.exception.EntityNotFoundException;
import com.irctc.payment.exception.InsufficientBalanceException;
import com.irctc.payment.exception.InvalidRequestException;
import com.irctc.payment.repository.WalletRepository;
import com.irctc.payment.repository.WalletTransactionRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wallet Service
 * Manages wallet operations: top-up, payments, transfers, and transaction history
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class WalletService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private WalletTransactionRepository transactionRepository;
    
    @Autowired(required = false)
    private SimplePaymentService paymentService;
    
    @Autowired(required = false)
    private GatewaySelectorService gatewaySelectorService;
    
    /**
     * Get or create wallet for user
     */
    @Transactional
    public Wallet getOrCreateWallet(String userId) {
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isPresent()) {
            return walletOpt.get();
        }
        
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setTotalTopUp(BigDecimal.ZERO);
        wallet.setTotalSpent(BigDecimal.ZERO);
        wallet.setIsActive(true);
        wallet.setTenantId(TenantContext.getTenantId());
        
        Wallet saved = walletRepository.save(wallet);
        logger.info("Created new wallet for user: {}", userId);
        return saved;
    }
    
    /**
     * Get wallet balance
     */
    @Transactional(readOnly = true)
    public WalletResponse getWalletBalance(String userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return convertToResponse(wallet);
    }
    
    /**
     * Top-up wallet (add money)
     */
    @Transactional
    public WalletTransactionResponse topUp(String userId, WalletTopUpRequest request) {
        logger.info("Processing wallet top-up for user: {}, amount: {}", userId, request.getAmount());
        
        // Get or create wallet
        Wallet wallet = getOrCreateWallet(userId);
        
        // Process payment for top-up
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setCurrency(request.getCurrency());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setDescription(request.getDescription() != null ? 
            request.getDescription() : "Wallet top-up");
        paymentRequest.setCustomerId(userId);
        paymentRequest.setGatewayPreference(request.getGatewayPreference());
        
        // Process payment through gateway
        SimplePayment payment = null;
        if (paymentService != null) {
            try {
                // Create a temporary payment entity for top-up
                SimplePayment tempPayment = new SimplePayment();
                tempPayment.setBookingId(0L); // Top-up doesn't have booking
                tempPayment.setAmount(request.getAmount().doubleValue());
                tempPayment.setCurrency(request.getCurrency());
                tempPayment.setPaymentMethod(request.getPaymentMethod());
                tempPayment.setStatus("PENDING");
                
                payment = paymentService.processPaymentWithGateway(tempPayment, request.getGatewayPreference());
                
                if (!"SUCCESS".equals(payment.getStatus()) && !"COMPLETED".equals(payment.getStatus())) {
                    throw new InvalidRequestException("Payment failed for wallet top-up");
                }
            } catch (Exception e) {
                logger.error("Payment processing failed for wallet top-up: {}", e.getMessage());
                throw new InvalidRequestException("Payment processing failed: " + e.getMessage());
            }
        }
        
        // Update wallet balance
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setTotalTopUp(wallet.getTotalTopUp().add(request.getAmount()));
        wallet.setLastTransactionAt(LocalDateTime.now());
        
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = createTransaction(
            wallet.getId(),
            userId,
            WalletTransaction.TransactionType.TOP_UP,
            request.getAmount(),
            oldBalance,
            newBalance,
            "Wallet top-up",
            payment != null ? payment.getTransactionId() : null,
            "TOP_UP"
        );
        
        transaction = transactionRepository.save(transaction);
        
        logger.info("Wallet top-up successful. User: {}, Amount: {}, New Balance: {}", 
            userId, request.getAmount(), newBalance);
        
        return convertTransactionToResponse(transaction);
    }
    
    /**
     * Make payment using wallet
     */
    @Transactional
    public WalletTransactionResponse makePayment(String userId, WalletPaymentRequest request) {
        logger.info("Processing wallet payment for user: {}, booking: {}, amount: {}", 
            userId, request.getBookingId(), request.getAmount());
        
        Wallet wallet = getOrCreateWallet(userId);
        
        // Check balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient wallet balance. Available: " + wallet.getBalance() + 
                ", Required: " + request.getAmount());
        }
        
        // Deduct amount
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.subtract(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setTotalSpent(wallet.getTotalSpent().add(request.getAmount()));
        wallet.setLastTransactionAt(LocalDateTime.now());
        
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = createTransaction(
            wallet.getId(),
            userId,
            WalletTransaction.TransactionType.PAYMENT,
            request.getAmount(),
            oldBalance,
            newBalance,
            request.getDescription() != null ? request.getDescription() : 
                "Payment for booking " + request.getBookingId(),
            request.getBookingId().toString(),
            "PAYMENT"
        );
        
        transaction = transactionRepository.save(transaction);
        
        logger.info("Wallet payment successful. User: {}, Booking: {}, Amount: {}, New Balance: {}", 
            userId, request.getBookingId(), request.getAmount(), newBalance);
        
        return convertTransactionToResponse(transaction);
    }
    
    /**
     * Transfer money to another wallet
     */
    @Transactional
    public WalletTransactionResponse transfer(String userId, WalletTransferRequest request) {
        logger.info("Processing wallet transfer from user: {} to user: {}, amount: {}", 
            userId, request.getRecipientUserId(), request.getAmount());
        
        if (userId.equals(request.getRecipientUserId())) {
            throw new InvalidRequestException("Cannot transfer to own wallet");
        }
        
        Wallet senderWallet = getOrCreateWallet(userId);
        
        // Check balance
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient wallet balance. Available: " + senderWallet.getBalance() + 
                ", Required: " + request.getAmount());
        }
        
        // Deduct from sender
        BigDecimal senderOldBalance = senderWallet.getBalance();
        BigDecimal senderNewBalance = senderOldBalance.subtract(request.getAmount());
        senderWallet.setBalance(senderNewBalance);
        senderWallet.setTotalSpent(senderWallet.getTotalSpent().add(request.getAmount()));
        senderWallet.setLastTransactionAt(LocalDateTime.now());
        senderWallet = walletRepository.save(senderWallet);
        
        // Add to recipient
        Wallet recipientWallet = getOrCreateWallet(request.getRecipientUserId());
        BigDecimal recipientOldBalance = recipientWallet.getBalance();
        BigDecimal recipientNewBalance = recipientOldBalance.add(request.getAmount());
        recipientWallet.setBalance(recipientNewBalance);
        recipientWallet.setTotalTopUp(recipientWallet.getTotalTopUp().add(request.getAmount()));
        recipientWallet.setLastTransactionAt(LocalDateTime.now());
        recipientWallet = walletRepository.save(recipientWallet);
        
        // Create transaction for sender
        WalletTransaction senderTransaction = createTransaction(
            senderWallet.getId(),
            userId,
            WalletTransaction.TransactionType.TRANSFER,
            request.getAmount(),
            senderOldBalance,
            senderNewBalance,
            request.getDescription() != null ? request.getDescription() : 
                "Transfer to user " + request.getRecipientUserId(),
            recipientWallet.getId().toString(),
            "TRANSFER"
        );
        transactionRepository.save(senderTransaction);
        
        // Create transaction for recipient
        WalletTransaction recipientTransaction = createTransaction(
            recipientWallet.getId(),
            request.getRecipientUserId(),
            WalletTransaction.TransactionType.TRANSFER_RECEIVED,
            request.getAmount(),
            recipientOldBalance,
            recipientNewBalance,
            request.getDescription() != null ? request.getDescription() : 
                "Transfer from user " + userId,
            senderWallet.getId().toString(),
            "TRANSFER"
        );
        transactionRepository.save(recipientTransaction);
        
        logger.info("Wallet transfer successful. From: {}, To: {}, Amount: {}", 
            userId, request.getRecipientUserId(), request.getAmount());
        
        return convertTransactionToResponse(senderTransaction);
    }
    
    /**
     * Add refund to wallet
     */
    @Transactional
    public WalletTransactionResponse addRefund(String userId, BigDecimal amount, String referenceId, String description) {
        logger.info("Adding refund to wallet. User: {}, Amount: {}, Reference: {}", 
            userId, amount, referenceId);
        
        Wallet wallet = getOrCreateWallet(userId);
        
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        wallet.setBalance(newBalance);
        wallet.setTotalTopUp(wallet.getTotalTopUp().add(amount));
        wallet.setLastTransactionAt(LocalDateTime.now());
        
        wallet = walletRepository.save(wallet);
        
        WalletTransaction transaction = createTransaction(
            wallet.getId(),
            userId,
            WalletTransaction.TransactionType.REFUND,
            amount,
            oldBalance,
            newBalance,
            description != null ? description : "Refund",
            referenceId,
            "REFUND"
        );
        
        transaction = transactionRepository.save(transaction);
        
        logger.info("Refund added to wallet. User: {}, Amount: {}, New Balance: {}", 
            userId, amount, newBalance);
        
        return convertTransactionToResponse(transaction);
    }
    
    /**
     * Add cashback to wallet
     */
    @Transactional
    public WalletTransactionResponse addCashback(String userId, BigDecimal amount, String description) {
        logger.info("Adding cashback to wallet. User: {}, Amount: {}", userId, amount);
        
        Wallet wallet = getOrCreateWallet(userId);
        
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        wallet.setBalance(newBalance);
        wallet.setTotalTopUp(wallet.getTotalTopUp().add(amount));
        wallet.setLastTransactionAt(LocalDateTime.now());
        
        wallet = walletRepository.save(wallet);
        
        WalletTransaction transaction = createTransaction(
            wallet.getId(),
            userId,
            WalletTransaction.TransactionType.CASHBACK,
            amount,
            oldBalance,
            newBalance,
            description != null ? description : "Cashback",
            null,
            "CASHBACK"
        );
        
        transaction = transactionRepository.save(transaction);
        
        logger.info("Cashback added to wallet. User: {}, Amount: {}, New Balance: {}", 
            userId, amount, newBalance);
        
        return convertTransactionToResponse(transaction);
    }
    
    /**
     * Get transaction history
     */
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionHistory(String userId, Pageable pageable) {
        Wallet wallet = getOrCreateWallet(userId);
        Page<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(
            wallet.getId(), pageable);
        return transactions.getContent().stream()
            .map(this::convertTransactionToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get transaction history by type
     */
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionHistoryByType(
            String userId, 
            WalletTransaction.TransactionType type) {
        return transactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, type)
            .stream()
            .map(this::convertTransactionToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public WalletTransactionResponse getTransaction(String transactionId) {
        WalletTransaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("WalletTransaction", transactionId));
        return convertTransactionToResponse(transaction);
    }
    
    /**
     * Helper method to create transaction
     */
    private WalletTransaction createTransaction(
            Long walletId,
            String userId,
            WalletTransaction.TransactionType type,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description,
            String referenceId,
            String referenceType) {
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setUserId(userId);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transaction.setStatus("SUCCESS");
        transaction.setTenantId(TenantContext.getTenantId());
        
        return transaction;
    }
    
    /**
     * Convert Wallet to WalletResponse
     */
    private WalletResponse convertToResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setTotalTopUp(wallet.getTotalTopUp());
        response.setTotalSpent(wallet.getTotalSpent());
        response.setIsActive(wallet.getIsActive());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setLastTransactionAt(wallet.getLastTransactionAt());
        return response;
    }
    
    /**
     * Convert WalletTransaction to WalletTransactionResponse
     */
    private WalletTransactionResponse convertTransactionToResponse(WalletTransaction transaction) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setId(transaction.getId());
        response.setWalletId(transaction.getWalletId());
        response.setUserId(transaction.getUserId());
        response.setTransactionType(transaction.getTransactionType());
        response.setAmount(transaction.getAmount());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTransactionId(transaction.getTransactionId());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setReferenceId(transaction.getReferenceId());
        response.setReferenceType(transaction.getReferenceType());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}

