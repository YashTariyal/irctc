package com.irctc.notification.tenant;

public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}

