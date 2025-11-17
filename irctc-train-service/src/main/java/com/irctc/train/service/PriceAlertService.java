package com.irctc.train.service;

import com.irctc.train.dto.PriceAlertRequest;
import com.irctc.train.dto.PriceAlertResponse;
import com.irctc.train.dto.PriceAlertUpdateRequest;
import com.irctc.train.entity.PriceAlert;
import com.irctc.train.exception.EntityNotFoundException;
import com.irctc.train.repository.PriceAlertRepository;
import com.irctc.train.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceAlertService {

    private static final Logger logger = LoggerFactory.getLogger(PriceAlertService.class);

    private final PriceAlertRepository priceAlertRepository;

    public PriceAlertService(PriceAlertRepository priceAlertRepository) {
        this.priceAlertRepository = priceAlertRepository;
    }

    @Transactional
    public PriceAlertResponse createAlert(PriceAlertRequest request) {
        PriceAlert alert = new PriceAlert();
        BeanUtils.copyProperties(request, alert);
        if (TenantContext.hasTenant()) {
            alert.setTenantId(TenantContext.getTenantId());
        }
        alert.setStatus("ACTIVE");
        PriceAlert saved = priceAlertRepository.save(alert);
        logger.info("✅ Price alert created for user {} - {}", saved.getUserId(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PriceAlertResponse getAlert(Long id) {
        PriceAlert alert = getAlertEntity(id);
        return toResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponse> getAlertsForUser(Long userId) {
        return priceAlertRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PriceAlertResponse updateAlert(Long id, PriceAlertUpdateRequest request) {
        PriceAlert alert = getAlertEntity(id);
        if (request.getAlertType() != null) {
            alert.setAlertType(request.getAlertType());
        }
        if (request.getNotificationChannel() != null) {
            alert.setNotificationChannel(request.getNotificationChannel());
        }
        if (request.getTargetPrice() != null) {
            alert.setTargetPrice(request.getTargetPrice());
        }
        if (request.getMinAvailability() != null) {
            alert.setMinAvailability(request.getMinAvailability());
        }
        if (request.getTravelDate() != null) {
            alert.setTravelDate(request.getTravelDate());
        }
        if (request.getTrainNumber() != null) {
            alert.setTrainNumber(request.getTrainNumber());
        }
        if (request.getSourceStation() != null) {
            alert.setSourceStation(request.getSourceStation());
        }
        if (request.getDestinationStation() != null) {
            alert.setDestinationStation(request.getDestinationStation());
        }
        if (request.getRecurrence() != null) {
            alert.setRecurrence(request.getRecurrence());
        }
        if (request.getStatus() != null) {
            alert.setStatus(request.getStatus());
        }
        PriceAlert saved = priceAlertRepository.save(alert);
        logger.info("✏️ Price alert {} updated", id);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAlert(Long id) {
        PriceAlert alert = getAlertEntity(id);
        priceAlertRepository.delete(alert);
        logger.info("🗑️ Price alert {} deleted", id);
    }

    @Transactional
    public PriceAlert markTriggered(PriceAlert alert) {
        alert.setStatus(alert.getRecurrence() != null && alert.getRecurrence().equalsIgnoreCase("RECURRING")
            ? "ACTIVE" : "TRIGGERED");
        alert.setLastTriggeredAt(LocalDateTime.now());
        return priceAlertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<PriceAlert> getActiveAlerts() {
        return priceAlertRepository.findByStatus("ACTIVE");
    }

    private PriceAlert getAlertEntity(Long id) {
        return priceAlertRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("PriceAlert", id));
    }

    private PriceAlertResponse toResponse(PriceAlert alert) {
        PriceAlertResponse response = new PriceAlertResponse();
        BeanUtils.copyProperties(alert, response);
        return response;
    }
}

