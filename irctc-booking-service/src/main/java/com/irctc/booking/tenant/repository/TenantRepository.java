package com.irctc.booking.tenant.repository;

import com.irctc.booking.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Tenant Repository
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    /**
     * Find tenant by code
     */
    Optional<Tenant> findByCode(String code);
    
    /**
     * Check if tenant exists by code
     */
    boolean existsByCode(String code);
    
    /**
     * Find active tenant by code
     */
    @Query("SELECT t FROM Tenant t WHERE t.code = :code AND t.status = 'ACTIVE'")
    Optional<Tenant> findActiveByCode(String code);
}

