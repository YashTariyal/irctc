package com.irctc.payment.repository;

import com.irctc.payment.entity.BiometricAuthorizationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiometricAuthorizationLogRepository extends JpaRepository<BiometricAuthorizationLog, Long> {
}

