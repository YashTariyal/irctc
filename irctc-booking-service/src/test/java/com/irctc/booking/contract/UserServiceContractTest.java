package com.irctc.booking.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract Test for Booking Service → User Service
 * 
 * This test verifies that Booking Service can correctly interact with User Service
 * according to the defined contract.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@ExtendWith(PactConsumerTestExt.class)
@Disabled("Pact 4.x API migration in progress - temporarily disabled")
public class UserServiceContractTest {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Contract: Get User by ID
     * 
     * Booking Service expects User Service to:
     * - Accept GET /api/users/{id}
     * - Return 200 OK with user object
     */
    @Pact(consumer = "booking-service", provider = "user-service")
    public RequestResponsePact getUserByIdPact(PactDslWithProvider builder) {
        return builder
            .given("user with id 1 exists")
            .uponReceiving("a request to get user by id")
            .path("/api/users/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                  "id": 1,
                  "username": "testuser",
                  "email": "test@example.com",
                  "firstName": "Test",
                  "lastName": "User",
                  "phoneNumber": "1234567890",
                  "roles": "USER"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserByIdPact")
    void testGetUserById(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/api/users/1";
        
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
        assertEquals("testuser", response.getBody().get("username"));
        assertEquals("test@example.com", response.getBody().get("email"));
    }

    /**
     * Contract: Get User by ID - Not Found
     */
    @Pact(consumer = "booking-service", provider = "user-service")
    public RequestResponsePact getUserByIdNotFoundPact(PactDslWithProvider builder) {
        return builder
            .given("user with id 999 does not exist")
            .uponReceiving("a request to get non-existent user")
            .path("/api/users/999")
            .method("GET")
            .willRespondWith()
            .status(404)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserByIdNotFoundPact")
    void testGetUserByIdNotFound(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/api/users/999";
        
        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            Map.class
        );
        
        // Then
        assertEquals(404, response.getStatusCode().value());
    }
}

