package com.irctc.user.repository;

import com.irctc.user.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {
    List<UserConsent> findByUserId(Long userId);
    
    Optional<UserConsent> findByUserIdAndConsentType(Long userId, String consentType);
    
    List<UserConsent> findByUserIdAndGrantedTrue(Long userId);
    
    @Query("SELECT c FROM UserConsent c WHERE c.userId = :userId AND c.consentType = :consentType AND c.granted = true")
    Optional<UserConsent> findActiveConsent(@Param("userId") Long userId, @Param("consentType") String consentType);
}

