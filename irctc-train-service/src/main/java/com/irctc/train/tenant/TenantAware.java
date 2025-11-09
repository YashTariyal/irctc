package com.irctc.train.tenant;

public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}

