package com.irctc.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "idx_idemp_key", columnList = "idempotencyKey", unique = true)
})
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false, length = 16)
    private String httpMethod;

    @Column(nullable = false, length = 255)
    private String requestPath;

    @Column(columnDefinition = "TEXT")
    private String requestHash;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(length = 64)
    private String responseStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
