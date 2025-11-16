package com.irctc.user.repository;

import com.irctc.user.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    
    List<SocialAccount> findByUserId(Long userId);
    
    List<SocialAccount> findByUserIdAndActiveTrue(Long userId);
    
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
    
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.userId = :userId AND sa.provider = :provider AND sa.active = true")
    Optional<SocialAccount> findByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
    
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.provider = :provider AND sa.providerUserId = :providerUserId AND sa.active = true")
    Optional<SocialAccount> findActiveByProviderAndProviderUserId(
        @Param("provider") String provider, 
        @Param("providerUserId") String providerUserId
    );
    
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}

