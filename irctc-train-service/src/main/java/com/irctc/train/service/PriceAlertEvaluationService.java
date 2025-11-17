package com.irctc.train.service;

import com.irctc.train.client.NotificationServiceClient;
import com.irctc.train.dto.PushNotificationRequest;
import com.irctc.train.entity.PriceAlert;
import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.repository.SimpleTrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PriceAlertEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(PriceAlertEvaluationService.class);

    private final PriceAlertService priceAlertService;
    private final SimpleTrainRepository trainRepository;
    private final NotificationServiceClient notificationClient;

    @Value("${price.alerts.notification.enabled:true}")
    private boolean notificationsEnabled;

    public PriceAlertEvaluationService(PriceAlertService priceAlertService,
                                       SimpleTrainRepository trainRepository,
                                       NotificationServiceClient notificationClient) {
        this.priceAlertService = priceAlertService;
        this.trainRepository = trainRepository;
        this.notificationClient = notificationClient;
    }

    @Scheduled(fixedDelayString = "${price.alerts.evaluation.delay:60000}")
    public void evaluateAlerts() {
        List<PriceAlert> alerts = priceAlertService.getActiveAlerts();
        if (alerts.isEmpty()) {
            return;
        }
        logger.debug("Evaluating {} price alerts", alerts.size());
        alerts.forEach(this::evaluateAlert);
    }

    private void evaluateAlert(PriceAlert alert) {
        Optional<SimpleTrain> trainOptional = resolveTrain(alert);
        if (trainOptional.isEmpty()) {
            logger.debug("No train data found for alert {}", alert.getId());
            return;
        }

        SimpleTrain train = trainOptional.get();
        boolean shouldTrigger = switch (alert.getAlertType()) {
            case "PRICE_DROP" -> meetsPriceCondition(alert, train);
            case "AVAILABILITY" -> meetsAvailabilityCondition(alert, train);
            default -> false;
        };

        if (!shouldTrigger) {
            return;
        }

        priceAlertService.markTriggered(alert);
        sendNotification(alert, train);
    }

    private Optional<SimpleTrain> resolveTrain(PriceAlert alert) {
        if (alert.getTrainNumber() != null && !alert.getTrainNumber().isBlank()) {
            return trainRepository.findByTrainNumber(alert.getTrainNumber());
        }

        if (alert.getSourceStation() != null && alert.getDestinationStation() != null) {
            List<SimpleTrain> trains = trainRepository.findBySourceStationAndDestinationStation(
                alert.getSourceStation(), alert.getDestinationStation());
            return trains.stream()
                .sorted(Comparator.comparing(SimpleTrain::getBaseFare))
                .findFirst();
        }

        return Optional.empty();
    }

    private boolean meetsPriceCondition(PriceAlert alert, SimpleTrain train) {
        if (alert.getTargetPrice() == null) {
            return false;
        }
        BigDecimal trainFare = BigDecimal.valueOf(train.getBaseFare());
        boolean result = trainFare.compareTo(alert.getTargetPrice()) <= 0;
        logger.debug("Price alert {} evaluation: train fare={} target={} -> {}", alert.getId(), trainFare, alert.getTargetPrice(), result);
        return result;
    }

    private boolean meetsAvailabilityCondition(PriceAlert alert, SimpleTrain train) {
        if (alert.getMinAvailability() == null) {
            return false;
        }
        boolean result = train.getAvailableSeats() >= alert.getMinAvailability();
        logger.debug("Availability alert {} evaluation: seats={} min={} -> {}", alert.getId(), train.getAvailableSeats(), alert.getMinAvailability(), result);
        return result;
    }

    private void sendNotification(PriceAlert alert, SimpleTrain train) {
        if (!notificationsEnabled) {
            logger.info("Notifications disabled. Price alert {} triggered but no notification sent.", alert.getId());
            return;
        }
        try {
            PushNotificationRequest request = new PushNotificationRequest();
            request.setUserId(alert.getUserId());
            request.setTitle("Price Alert Matched");
            request.setNotificationType("PRICE_ALERT");
            request.setBody(buildNotificationBody(alert, train));
            request.setData(buildMetadata(alert, train));
            notificationClient.sendPushNotification(request);
            logger.info("📣 Price alert notification sent for alert {}", alert.getId());
        } catch (Exception ex) {
            logger.error("Failed to send price alert notification for alert {}: {}", alert.getId(), ex.getMessage());
        }
    }

    private String buildNotificationBody(PriceAlert alert, SimpleTrain train) {
        if ("PRICE_DROP".equals(alert.getAlertType())) {
            return String.format("Fare for %s is now ₹%.2f. Your target was ₹%.2f.",
                train.getTrainNumber(), train.getBaseFare(), alert.getTargetPrice());
        }
        if ("AVAILABILITY".equals(alert.getAlertType())) {
            return String.format("Seats available for %s: %d", train.getTrainNumber(), train.getAvailableSeats());
        }
        return "A saved alert condition has been met.";
    }

    private java.util.Map<String, String> buildMetadata(PriceAlert alert, SimpleTrain train) {
        return java.util.Map.of(
            "alertId", String.valueOf(alert.getId()),
            "trainNumber", train.getTrainNumber(),
            "source", train.getSourceStation(),
            "destination", train.getDestinationStation(),
            "alertType", alert.getAlertType()
        );
    }
}

