package com.irctc.booking.config;

import com.irctc.booking.websocket.BookingStatusHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration
 * 
 * Enables WebSocket support for real-time booking status updates.
 * Clients can subscribe to booking status changes via WebSocket connections.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private BookingStatusHandler bookingStatusHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(bookingStatusHandler, "/ws/bookings")
                .setAllowedOrigins("*") // Configure appropriately for production
                .withSockJS(); // Enable SockJS fallback for better browser compatibility
    }
}

