package com.irctc.user.tenant.repository;

import com.irctc.user.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByCode(String code);
    boolean existsByCode(String code);
    
    @Query("SELECT t FROM Tenant t WHERE t.code = :code AND t.status = 'ACTIVE'")
    Optional<Tenant> findActiveByCode(String code);
}

