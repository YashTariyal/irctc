package com.irctc.booking.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Saga Instances
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
    
    Optional<SagaInstance> findBySagaId(String sagaId);
    
    Optional<SagaInstance> findByCorrelationId(String correlationId);
}

