package com.irctc.booking.tenant.filter;

import com.irctc.booking.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

/**
 * Tenant Filter
 * 
 * Hibernate filter for automatic tenant-based data filtering
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class TenantFilter {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @PostConstruct
    public void registerFilter() {
        // Filter will be enabled per transaction
    }
    
    /**
     * Enable tenant filter for current session
     */
    @Transactional
    public void enableTenantFilter() {
        if (TenantContext.hasTenant()) {
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", TenantContext.getTenantId());
        }
    }
    
    /**
     * Disable tenant filter
     */
    @Transactional
    public void disableTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");
    }
}

