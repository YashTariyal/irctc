package com.irctc.payment.tenant;

public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}

