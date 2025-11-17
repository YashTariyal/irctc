package com.irctc.user.repository;

import com.irctc.user.entity.UserReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReferralRepository extends JpaRepository<UserReferral, Long> {
    List<UserReferral> findByReferrerUserIdOrderByCreatedAtDesc(Long referrerUserId);
    Optional<UserReferral> findByReferredUserId(Long referredUserId);
    long countByReferrerUserId(Long referrerUserId);
}

