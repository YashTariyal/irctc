package com.irctc.user.tenant;

public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}

