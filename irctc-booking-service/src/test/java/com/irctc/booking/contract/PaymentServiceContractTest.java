package com.irctc.booking.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract Test for Booking Service → Payment Service
 * 
 * This test verifies that Booking Service can correctly interact with Payment Service
 * according to the defined contract.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@ExtendWith(PactConsumerTestExt.class)
public class PaymentServiceContractTest {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Contract: Create Payment
     * 
     * Booking Service expects Payment Service to:
     * - Accept POST /api/payments with payment data
     * - Return 200 OK with payment object including id, status, transactionId
     */
    @Pact(consumer = "booking-service", provider = "payment-service")
    public RequestResponsePact createPaymentPact(PactDslWithProvider builder) {
        return builder
            .given("payment service is available")
            .uponReceiving("a request to create payment")
            .path("/api/payments")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body("""
                {
                  "bookingId": 123,
                  "amount": 1000.0,
                  "currency": "INR",
                  "paymentMethod": "CREDIT_CARD",
                  "status": "PENDING"
                }
                """)
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                  "id": 1,
                  "bookingId": 123,
                  "amount": 1000.0,
                  "currency": "INR",
                  "paymentMethod": "CREDIT_CARD",
                  "status": "SUCCESS",
                  "transactionId": "TXN123456789",
                  "paymentTime": "2024-11-09T10:00:00"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPaymentPact")
    void testCreatePayment(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/api/payments";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("bookingId", 123L);
        paymentRequest.put("amount", 1000.0);
        paymentRequest.put("currency", "INR");
        paymentRequest.put("paymentMethod", "CREDIT_CARD");
        paymentRequest.put("status", "PENDING");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentRequest, headers);
        
        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            Map.class
        );
        
        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, ((Number) response.getBody().get("id")).longValue());
        assertEquals(123L, ((Number) response.getBody().get("bookingId")).longValue());
        assertEquals(1000.0, ((Number) response.getBody().get("amount")).doubleValue());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertNotNull(response.getBody().get("transactionId"));
    }

    /**
     * Contract: Get Payment by ID
     */
    @Pact(consumer = "booking-service", provider = "payment-service")
    public RequestResponsePact getPaymentByIdPact(PactDslWithProvider builder) {
        return builder
            .given("payment with id 1 exists")
            .uponReceiving("a request to get payment by id")
            .path("/api/payments/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                  "id": 1,
                  "bookingId": 123,
                  "amount": 1000.0,
                  "currency": "INR",
                  "paymentMethod": "CREDIT_CARD",
                  "status": "SUCCESS",
                  "transactionId": "TXN123456789",
                  "paymentTime": "2024-11-09T10:00:00"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getPaymentByIdPact")
    void testGetPaymentById(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/api/payments/1";
        
        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            Map.class
        );
        
        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, ((Number) response.getBody().get("id")).longValue());
        assertEquals("SUCCESS", response.getBody().get("status"));
    }

    /**
     * Contract: Get Payments by Booking ID
     */
    @Pact(consumer = "booking-service", provider = "payment-service")
    public RequestResponsePact getPaymentsByBookingIdPact(PactDslWithProvider builder) {
        return builder
            .given("payments exist for booking 123")
            .uponReceiving("a request to get payments by booking id")
            .path("/api/payments/booking/123")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                [
                  {
                    "id": 1,
                    "bookingId": 123,
                    "amount": 1000.0,
                    "currency": "INR",
                    "paymentMethod": "CREDIT_CARD",
                    "status": "SUCCESS",
                    "transactionId": "TXN123456789",
                    "paymentTime": "2024-11-09T10:00:00"
                  }
                ]
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getPaymentsByBookingIdPact")
    void testGetPaymentsByBookingId(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/api/payments/booking/123";
        
        // When
        ResponseEntity<Map[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            Map[].class
        );
        
        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertEquals(123L, ((Number) response.getBody()[0].get("bookingId")).longValue());
    }
}

