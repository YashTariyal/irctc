package com.irctc.booking.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.booking.entity.SimpleBooking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for Real-Time Booking Status Updates
 * 
 * Manages WebSocket connections and broadcasts booking status updates
 * to subscribed clients in real-time.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class BookingStatusHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(BookingStatusHandler.class);
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> userSubscriptions = new ConcurrentHashMap<>(); // sessionId -> userId
    private final ObjectMapper objectMapper;
    
    public BookingStatusHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("✅ WebSocket connection established: {}", sessionId);
        
        // Send welcome message
        sendMessage(session, createMessage("connected", "WebSocket connection established"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        userSubscriptions.remove(sessionId);
        logger.info("❌ WebSocket connection closed: {} - Status: {}", sessionId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        logger.debug("📨 Received WebSocket message from {}: {}", sessionId, payload);
        
        try {
            // Parse subscription message
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String action = (String) request.get("action");
            
            if ("subscribe".equals(action)) {
                // Subscribe to a specific user's bookings
                Object userIdObj = request.get("userId");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    userSubscriptions.put(sessionId, userId);
                    logger.info("📋 Session {} subscribed to user {} bookings", sessionId, userId);
                    sendMessage(session, createMessage("subscribed", "Subscribed to booking updates for user: " + userId));
                }
            } else if ("unsubscribe".equals(action)) {
                userSubscriptions.remove(sessionId);
                logger.info("📋 Session {} unsubscribed", sessionId);
                sendMessage(session, createMessage("unsubscribed", "Unsubscribed from booking updates"));
            } else {
                sendMessage(session, createMessage("error", "Unknown action: " + action));
            }
        } catch (Exception e) {
            logger.error("❌ Error handling WebSocket message", e);
            sendMessage(session, createMessage("error", "Invalid message format"));
        }
    }

    /**
     * Broadcast booking status update to subscribed clients
     */
    public void broadcastBookingUpdate(SimpleBooking booking) {
        String bookingJson;
        try {
            bookingJson = objectMapper.writeValueAsString(booking);
        } catch (Exception e) {
            logger.error("❌ Error serializing booking", e);
            return;
        }

        Long bookingUserId = booking.getUserId();
        int broadcastCount = 0;

        for (Map.Entry<String, Long> subscription : userSubscriptions.entrySet()) {
            String sessionId = subscription.getKey();
            Long subscribedUserId = subscription.getValue();

            // Only send to clients subscribed to this user's bookings
            if (bookingUserId.equals(subscribedUserId)) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        Map<String, Object> message = Map.of(
                            "type", "booking_update",
                            "booking", objectMapper.readValue(bookingJson, Map.class)
                        );
                        sendMessage(session, objectMapper.writeValueAsString(message));
                        broadcastCount++;
                    } catch (Exception e) {
                        logger.error("❌ Error sending booking update to session {}", sessionId, e);
                    }
                }
            }
        }

        if (broadcastCount > 0) {
            logger.info("📤 Broadcast booking update for booking {} to {} subscribers", 
                booking.getId(), broadcastCount);
        }
    }

    /**
     * Send a message to a specific session
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            logger.error("❌ Error sending WebSocket message", e);
        }
    }

    /**
     * Create a simple message JSON
     */
    private String createMessage(String type, String message) {
        try {
            Map<String, Object> msg = Map.of(
                "type", type,
                "message", message,
                "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            logger.error("❌ Error creating message", e);
            return "{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}";
        }
    }

    /**
     * Get number of active WebSocket connections
     */
    public int getActiveConnections() {
        return sessions.size();
    }
}

