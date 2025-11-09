package com.irctc.payment.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.SimplePaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Provider Verification Test for Payment Service
 * 
 * This test verifies that Payment Service fulfills the contracts
 * defined by consumers (e.g., Booking Service).
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("payment-service")
@PactFolder("src/test/resources/pacts")
@ExtendWith(PactVerificationInvocationContextProvider.class)
public class PaymentServiceProviderTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SimplePaymentRepository paymentRepository;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /**
     * State: payment service is available
     */
    @State("payment service is available")
    void paymentServiceAvailable() {
        // Setup: Ensure payment service is ready
        // In a real scenario, you might set up test data here
    }

    /**
     * State: payment with id 1 exists
     */
    @State("payment with id 1 exists")
    void paymentExists() {
        // Setup: Create a payment with id 1
        SimplePayment payment = new SimplePayment();
        payment.setId(1L);
        payment.setBookingId(123L);
        payment.setAmount(1000.0);
        payment.setCurrency("INR");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN123456789");
        payment.setPaymentTime(LocalDateTime.now());
        
        paymentRepository.save(payment);
    }

    /**
     * State: payments exist for booking 123
     */
    @State("payments exist for booking 123")
    void paymentsExistForBooking() {
        // Setup: Create payments for booking 123
        SimplePayment payment = new SimplePayment();
        payment.setBookingId(123L);
        payment.setAmount(1000.0);
        payment.setCurrency("INR");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN123456789");
        payment.setPaymentTime(LocalDateTime.now());
        
        paymentRepository.save(payment);
    }
}

