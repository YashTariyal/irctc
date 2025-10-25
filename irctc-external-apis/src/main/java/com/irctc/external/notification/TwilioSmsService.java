package com.irctc.external.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Twilio SMS Service Integration
 */
@Service
public class TwilioSmsService {

    private final WebClient webClient;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFromNumber;
    private final String twilioBaseUrl;

    public TwilioSmsService(WebClient.Builder webClientBuilder,
                           @Value("${twilio.account.sid}") String twilioAccountSid,
                           @Value("${twilio.auth.token}") String twilioAuthToken,
                           @Value("${twilio.from.number}") String twilioFromNumber,
                           @Value("${twilio.base.url:https://api.twilio.com/2010-04-01}") String twilioBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(twilioBaseUrl)
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(twilioAccountSid, twilioAuthToken);
                    headers.set("Content-Type", "application/x-www-form-urlencoded");
                })
                .build();
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioFromNumber = twilioFromNumber;
        this.twilioBaseUrl = twilioBaseUrl;
    }

    /**
     * Send SMS using Twilio
     */
    public Mono<TwilioSmsResponse> sendSms(String toNumber, String message) {
        SmsRequest request = new SmsRequest(twilioFromNumber, toNumber, message);
        
        return webClient.post()
                .uri("/Accounts/" + twilioAccountSid + "/Messages.json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TwilioSmsResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error sending SMS via Twilio: " + throwable.getMessage());
                    return Mono.just(new TwilioSmsResponse(false, "Error sending SMS"));
                });
    }

    /**
     * Send OTP SMS
     */
    public Mono<TwilioSmsResponse> sendOtpSms(String toNumber, String otp) {
        String message = "Your IRCTC OTP is: " + otp + ". Valid for 5 minutes. Do not share with anyone.";
        return sendSms(toNumber, message);
    }

    /**
     * Send booking confirmation SMS
     */
    public Mono<TwilioSmsResponse> sendBookingConfirmationSms(String toNumber, String passengerName, 
                                                             String pnrNumber, String trainName) {
        String message = String.format("""
            Hi %s! Your IRCTC booking is confirmed.
            PNR: %s
            Train: %s
            Thank you for choosing IRCTC!
            """, passengerName, pnrNumber, trainName);
        
        return sendSms(toNumber, message);
    }

    /**
     * Send payment confirmation SMS
     */
    public Mono<TwilioSmsResponse> sendPaymentConfirmationSms(String toNumber, String amount, 
                                                            String transactionId) {
        String message = String.format("""
            Payment successful!
            Amount: ₹%s
            Transaction ID: %s
            Thank you for your payment!
            """, amount, transactionId);
        
        return sendSms(toNumber, message);
    }

    /**
     * Send cancellation SMS
     */
    public Mono<TwilioSmsResponse> sendCancellationSms(String toNumber, String pnrNumber, 
                                                     String refundAmount) {
        String message = String.format("""
            Your booking has been cancelled.
            PNR: %s
            Refund Amount: ₹%s
            Refund will be processed in 5-7 business days.
            """, pnrNumber, refundAmount);
        
        return sendSms(toNumber, message);
    }

    // DTOs
    public static class SmsRequest {
        @JsonProperty("From")
        private String from;
        
        @JsonProperty("To")
        private String to;
        
        @JsonProperty("Body")
        private String body;

        public SmsRequest() {}

        public SmsRequest(String from, String to, String body) {
            this.from = from;
            this.to = to;
            this.body = body;
        }

        // Getters and Setters
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class TwilioSmsResponse {
        @JsonProperty("success")
        private boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("sid")
        private String sid;
        
        @JsonProperty("status")
        private String status;

        public TwilioSmsResponse() {}

        public TwilioSmsResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSid() { return sid; }
        public void setSid(String sid) { this.sid = sid; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
