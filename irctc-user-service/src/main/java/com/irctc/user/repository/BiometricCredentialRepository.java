package com.irctc.user.repository;

import com.irctc.user.entity.BiometricCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricCredentialRepository extends JpaRepository<BiometricCredential, Long> {

    Optional<BiometricCredential> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<BiometricCredential> findByUserId(Long userId);
}

