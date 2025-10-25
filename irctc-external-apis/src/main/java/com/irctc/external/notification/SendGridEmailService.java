package com.irctc.external.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * SendGrid Email Service Integration
 */
@Service
public class SendGridEmailService {

    private final WebClient webClient;
    private final String sendGridApiKey;
    private final String sendGridBaseUrl;

    public SendGridEmailService(WebClient.Builder webClientBuilder,
                               @Value("${sendgrid.api.key}") String sendGridApiKey,
                               @Value("${sendgrid.base.url:https://api.sendgrid.com/v3}") String sendGridBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(sendGridBaseUrl)
                .defaultHeaders(headers -> {
                    headers.set("Authorization", "Bearer " + sendGridApiKey);
                    headers.set("Content-Type", "application/json");
                })
                .build();
        this.sendGridApiKey = sendGridApiKey;
        this.sendGridBaseUrl = sendGridBaseUrl;
    }

    /**
     * Send email using SendGrid
     */
    public Mono<SendGridResponse> sendEmail(EmailRequest request) {
        return webClient.post()
                .uri("/mail/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SendGridResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error sending email via SendGrid: " + throwable.getMessage());
                    return Mono.just(new SendGridResponse(false, "Error sending email"));
                });
    }

    /**
     * Send booking confirmation email
     */
    public Mono<SendGridResponse> sendBookingConfirmation(String toEmail, String passengerName, 
                                                         String pnrNumber, String trainName, 
                                                         String journeyDate) {
        EmailRequest request = new EmailRequest();
        request.setFrom(new EmailAddress("noreply@irctc.com", "IRCTC"));
        request.addTo(new EmailAddress(toEmail, passengerName));
        request.setSubject("Booking Confirmation - PNR: " + pnrNumber);
        
        // Create HTML content
        String htmlContent = createBookingConfirmationHtml(passengerName, pnrNumber, trainName, journeyDate);
        request.setContent(new EmailContent("text/html", htmlContent));
        
        return sendEmail(request);
    }

    /**
     * Send payment confirmation email
     */
    public Mono<SendGridResponse> sendPaymentConfirmation(String toEmail, String passengerName, 
                                                        String amount, String transactionId) {
        EmailRequest request = new EmailRequest();
        request.setFrom(new EmailAddress("noreply@irctc.com", "IRCTC"));
        request.addTo(new EmailAddress(toEmail, passengerName));
        request.setSubject("Payment Confirmation - Transaction: " + transactionId);
        
        String htmlContent = createPaymentConfirmationHtml(passengerName, amount, transactionId);
        request.setContent(new EmailContent("text/html", htmlContent));
        
        return sendEmail(request);
    }

    /**
     * Send cancellation email
     */
    public Mono<SendGridResponse> sendCancellationEmail(String toEmail, String passengerName, 
                                                        String pnrNumber, String refundAmount) {
        EmailRequest request = new EmailRequest();
        request.setFrom(new EmailAddress("noreply@irctc.com", "IRCTC"));
        request.addTo(new EmailAddress(toEmail, passengerName));
        request.setSubject("Booking Cancelled - PNR: " + pnrNumber);
        
        String htmlContent = createCancellationHtml(passengerName, pnrNumber, refundAmount);
        request.setContent(new EmailContent("text/html", htmlContent));
        
        return sendEmail(request);
    }

    private String createBookingConfirmationHtml(String passengerName, String pnrNumber, 
                                                String trainName, String journeyDate) {
        return String.format("""
            <html>
            <body>
                <h2>Booking Confirmation</h2>
                <p>Dear %s,</p>
                <p>Your booking has been confirmed!</p>
                <ul>
                    <li><strong>PNR Number:</strong> %s</li>
                    <li><strong>Train:</strong> %s</li>
                    <li><strong>Journey Date:</strong> %s</li>
                </ul>
                <p>Thank you for choosing IRCTC!</p>
            </body>
            </html>
            """, passengerName, pnrNumber, trainName, journeyDate);
    }

    private String createPaymentConfirmationHtml(String passengerName, String amount, String transactionId) {
        return String.format("""
            <html>
            <body>
                <h2>Payment Confirmation</h2>
                <p>Dear %s,</p>
                <p>Your payment has been processed successfully!</p>
                <ul>
                    <li><strong>Amount:</strong> ₹%s</li>
                    <li><strong>Transaction ID:</strong> %s</li>
                </ul>
                <p>Thank you for your payment!</p>
            </body>
            </html>
            """, passengerName, amount, transactionId);
    }

    private String createCancellationHtml(String passengerName, String pnrNumber, String refundAmount) {
        return String.format("""
            <html>
            <body>
                <h2>Booking Cancelled</h2>
                <p>Dear %s,</p>
                <p>Your booking has been cancelled.</p>
                <ul>
                    <li><strong>PNR Number:</strong> %s</li>
                    <li><strong>Refund Amount:</strong> ₹%s</li>
                </ul>
                <p>Refund will be processed within 5-7 business days.</p>
            </body>
            </html>
            """, passengerName, pnrNumber, refundAmount);
    }

    // DTOs
    public static class EmailRequest {
        @JsonProperty("from")
        private EmailAddress from;
        
        @JsonProperty("personalizations")
        private List<Personalization> personalizations;
        
        @JsonProperty("subject")
        private String subject;
        
        @JsonProperty("content")
        private List<EmailContent> content;

        public EmailRequest() {}

        public void setFrom(EmailAddress from) {
            this.from = from;
        }

        public void addTo(EmailAddress to) {
            if (personalizations == null) {
                personalizations = new java.util.ArrayList<>();
                personalizations.add(new Personalization());
            }
            personalizations.get(0).addTo(to);
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setContent(EmailContent content) {
            this.content = new java.util.ArrayList<>();
            this.content.add(content);
        }

        // Getters and Setters
        public EmailAddress getFrom() { return from; }
        public List<Personalization> getPersonalizations() { return personalizations; }
        public void setPersonalizations(List<Personalization> personalizations) { this.personalizations = personalizations; }
        public String getSubject() { return subject; }
        public List<EmailContent> getContent() { return content; }
        public void setContent(List<EmailContent> content) { this.content = content; }
    }

    public static class EmailAddress {
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("name")
        private String name;

        public EmailAddress() {}

        public EmailAddress(String email, String name) {
            this.email = email;
            this.name = name;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Personalization {
        @JsonProperty("to")
        private List<EmailAddress> to;

        public Personalization() {
            this.to = new java.util.ArrayList<>();
        }

        public void addTo(EmailAddress to) {
            this.to.add(to);
        }

        // Getters and Setters
        public List<EmailAddress> getTo() { return to; }
        public void setTo(List<EmailAddress> to) { this.to = to; }
    }

    public static class EmailContent {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("value")
        private String value;

        public EmailContent() {}

        public EmailContent(String type, String value) {
            this.type = type;
            this.value = value;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class SendGridResponse {
        @JsonProperty("success")
        private boolean success;
        
        @JsonProperty("message")
        private String message;

        public SendGridResponse() {}

        public SendGridResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
