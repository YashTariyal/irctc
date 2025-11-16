package com.irctc.payment.repository;

import com.irctc.payment.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {
    
    List<RefundPolicy> findByActiveTrue();
    
    @Query("SELECT r FROM RefundPolicy r WHERE r.active = true ORDER BY r.priority ASC")
    List<RefundPolicy> findActivePoliciesOrderedByPriority();
    
    @Query("SELECT r FROM RefundPolicy r WHERE r.active = true AND r.tenantId = :tenantId ORDER BY r.priority ASC")
    List<RefundPolicy> findActivePoliciesByTenantOrderedByPriority(@Param("tenantId") String tenantId);
}

