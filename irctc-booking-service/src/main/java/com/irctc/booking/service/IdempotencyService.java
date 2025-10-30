package com.irctc.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.IdempotencyKey;
import com.irctc.booking.repository.IdempotencyKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);

    @Autowired
    private IdempotencyKeyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public <T> T process(String idempotencyKey,
                         String method,
                         String path,
                         Object requestBody,
                         Class<T> responseType,
                         Supplier<T> action) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        Optional<IdempotencyKey> existingOpt = repository.lockByKey(idempotencyKey);
        if (existingOpt.isPresent()) {
            IdempotencyKey existing = existingOpt.get();
            if (existing.getResponseBody() != null) {
                try {
                    T restored = objectMapper.readValue(existing.getResponseBody(), responseType);
                    logger.info("♻️ Idempotent replay for key={} path={}", idempotencyKey, path);
                    return restored;
                } catch (Exception e) {
                    logger.warn("Failed to deserialize stored idempotent response; proceeding to action", e);
                }
            } else {
                // In-flight or reserved. Proceed to execute once and store.
            }
        } else {
            // Reserve the key
            IdempotencyKey toSave = new IdempotencyKey();
            toSave.setIdempotencyKey(idempotencyKey);
            toSave.setHttpMethod(method);
            toSave.setRequestPath(path);
            toSave.setRequestHash(hashBody(requestBody));
            repository.save(toSave);
        }

        // Execute action and store result
        T result = action.get();
        try {
            String bodyJson = objectMapper.writeValueAsString(result);
            IdempotencyKey stored = repository.lockByKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency key lost during processing"));
            stored.setResponseBody(bodyJson);
            stored.setResponseStatus("200");
            stored.setCompletedAt(LocalDateTime.now());
            repository.save(stored);
        } catch (Exception e) {
            logger.error("Failed to store idempotent response for key=" + idempotencyKey, e);
        }
        return result;
    }

    private String hashBody(Object body) {
        try {
            if (body == null) return null;
            String json = objectMapper.writeValueAsString(body);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return null;
        }
    }
}
