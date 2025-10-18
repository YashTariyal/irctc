package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.PaymentCallbackRequest;
import com.irctc_backend.irctc.dto.PaymentRequest;
import com.irctc_backend.irctc.dto.PaymentResponse;
import com.irctc_backend.irctc.entity.Payment;
import com.irctc_backend.irctc.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment Controller
 * 
 * This controller handles all payment-related REST endpoints including
 * payment initiation, status checking, callback processing, and refunds.
 * It provides a comprehensive API for payment operations in the IRCTC system.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Payment Management", description = "APIs for managing payments, refunds, and payment gateway integration")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Initiate payment for a booking
     * 
     * @param paymentRequest Payment request details
     * @return PaymentResponse with payment initiation details
     */
    @PostMapping("/initiate")
    @Operation(
        summary = "💳 Initiate Payment", 
        description = """
            Initiates a payment for a booking through the integrated payment gateway.
            
            **Supported Payment Methods:**
            - Credit Card, Debit Card
            - Net Banking
            - UPI (Google Pay, PhonePe, Paytm, BHIM UPI)
            - Digital Wallets (Amazon Pay, etc.)
            
            **Process:**
            1. Validates booking and payment details
            2. Creates payment record in database
            3. Initiates payment with Razorpay gateway
            4. Returns payment URL and order details
            
            **Response includes:**
            - Payment ID and Transaction ID
            - Gateway Order ID
            - Payment URL for redirection
            - QR Code for UPI payments
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment initiated successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment request or booking not found"),
        @ApiResponse(responseCode = "409", description = "Booking already paid"),
        @ApiResponse(responseCode = "500", description = "Payment gateway error")
    })
    public ResponseEntity<?> initiatePayment(
            @Parameter(description = "Payment request details", required = true)
            @Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentResponse response = paymentService.initiatePayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Process payment callback from gateway
     * 
     * @param callbackRequest Callback request from payment gateway
     * @return PaymentResponse with updated payment status
     */
    @PostMapping("/callback")
    @Operation(
        summary = "🔄 Process Payment Callback", 
        description = """
            Processes payment callbacks from the payment gateway after payment completion.
            This endpoint is called by Razorpay after payment processing.
            
            **Callback Processing:**
            1. Verifies payment signature for security
            2. Updates payment status in database
            3. Updates booking payment status
            4. Sends notification to user
            5. Handles failed payments with retry logic
            
            **Security:**
            - Signature verification to prevent fraud
            - Idempotent processing to handle duplicate callbacks
            - Comprehensive error handling and logging
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid callback data or signature"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Callback processing error")
    })
    public ResponseEntity<?> processPaymentCallback(
            @Parameter(description = "Payment callback data from gateway", required = true)
            @RequestBody PaymentCallbackRequest callbackRequest) {
        try {
            PaymentResponse response = paymentService.processPaymentCallback(callbackRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get payment status
     * 
     * @param paymentId Payment ID
     * @return PaymentResponse with current payment status
     */
    @GetMapping("/{paymentId}/status")
    @Operation(
        summary = "📊 Get Payment Status", 
        description = """
            Retrieves the current status of a payment transaction.
            
            **Status Types:**
            - PENDING: Payment initiated but not completed
            - COMPLETED: Payment successful
            - FAILED: Payment failed
            - REFUNDED: Payment refunded
            - PARTIALLY_REFUNDED: Partial refund processed
            - CANCELLED: Payment cancelled
            
            **Use Cases:**
            - Check payment status after redirection
            - Monitor payment processing
            - Handle payment timeouts
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<?> getPaymentStatus(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Retry failed payment
     * 
     * @param paymentId Payment ID
     * @return PaymentResponse with retry details
     */
    @PostMapping("/{paymentId}/retry")
    @Operation(
        summary = "🔄 Retry Failed Payment", 
        description = """
            Retries a failed payment transaction.
            
            **Retry Conditions:**
            - Payment must be in FAILED status
            - Retry count must be less than maximum attempts
            - Payment must be within retry time window
            
            **Retry Process:**
            1. Validates retry eligibility
            2. Resets payment status to PENDING
            3. Increments retry counter
            4. Initiates new payment attempt
            5. Returns new payment details
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retry initiated successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Payment cannot be retried"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<?> retryPayment(
            @Parameter(description = "Payment ID to retry", required = true)
            @PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentService.retryPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Process refund for a payment
     * 
     * @param paymentId Payment ID
     * @param refundAmount Refund amount
     * @param refundReason Refund reason
     * @return PaymentResponse with refund details
     */
    @PostMapping("/{paymentId}/refund")
    @Operation(
        summary = "💰 Process Refund", 
        description = """
            Processes a refund for a completed payment.
            
            **Refund Types:**
            - Full Refund: Complete amount refunded
            - Partial Refund: Partial amount refunded
            
            **Refund Process:**
            1. Validates payment eligibility for refund
            2. Processes refund through payment gateway
            3. Updates payment status
            4. Sends refund notification
            5. Updates booking status if applicable
            
            **Refund Rules:**
            - Only completed payments can be refunded
            - Refund amount cannot exceed payment amount
            - Refund reason is required for audit trail
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<?> processRefund(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Long paymentId,
            @Parameter(description = "Refund amount", required = true)
            @RequestParam BigDecimal refundAmount,
            @Parameter(description = "Refund reason", required = true)
            @RequestParam String refundReason) {
        try {
            PaymentResponse response = paymentService.processRefund(paymentId, refundAmount, refundReason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get payments for a booking
     * 
     * @param bookingId Booking ID
     * @return List of payments for the booking
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(
        summary = "📋 Get Payments for Booking", 
        description = """
            Retrieves all payment transactions for a specific booking.
            
            **Response includes:**
            - All payment attempts (successful and failed)
            - Payment status and details
            - Transaction history
            - Refund information if applicable
            
            **Use Cases:**
            - View payment history for a booking
            - Track payment attempts
            - Audit payment transactions
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = Payment.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<?> getPaymentsForBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long bookingId) {
        try {
            List<Payment> payments = paymentService.getPaymentsForBooking(bookingId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get payment gateway configuration for frontend
     * 
     * @return Payment gateway configuration
     */
    @GetMapping("/config")
    @Operation(
        summary = "⚙️ Get Payment Gateway Config", 
        description = """
            Retrieves payment gateway configuration for frontend integration.
            
            **Configuration includes:**
            - Razorpay key ID for frontend
            - Supported payment methods
            - Currency options
            - Gateway-specific settings
            
            **Security Note:**
            - Only public configuration is returned
            - Secret keys are never exposed
            - Configuration is environment-specific
            """,
        tags = {"💳 Payments"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully")
    })
    public ResponseEntity<?> getPaymentConfig() {
        try {
            // Return payment gateway configuration for frontend
            return ResponseEntity.ok(new Object() {
                public final String razorpayKeyId = "rzp_test_1234567890"; // This should come from config
                public final String[] supportedMethods = {"card", "netbanking", "upi", "wallet"};
                public final String[] supportedCurrencies = {"INR"};
                public final String defaultCurrency = "INR";
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving payment configuration");
        }
    }
}
