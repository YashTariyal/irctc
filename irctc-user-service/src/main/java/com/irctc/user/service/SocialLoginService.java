package com.irctc.user.service;

import com.irctc.user.dto.LinkSocialAccountRequest;
import com.irctc.user.dto.LinkedAccountResponse;
import com.irctc.user.dto.SocialLoginRequest;
import com.irctc.user.dto.SocialLoginResponse;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.SocialAccount;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.SocialAccountRepository;
import com.irctc.user.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for social login integration (Google, Facebook, Apple)
 */
@Service
public class SocialLoginService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocialLoginService.class);
    
    @Autowired
    private SimpleUserRepository userRepository;
    
    @Autowired
    private SocialAccountRepository socialAccountRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Value("${social.login.google.client-id:}")
    private String googleClientId;
    
    @Value("${social.login.facebook.app-id:}")
    private String facebookAppId;
    
    @Value("${social.login.apple.client-id:}")
    private String appleClientId;
    
    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }
    
    /**
     * Authenticate user with Google OAuth2
     */
    @Transactional
    public SocialLoginResponse authenticateWithGoogle(SocialLoginRequest request) {
        logger.info("Authenticating user with Google");
        
        try {
            // Verify token and get user info from Google
            GoogleUserInfo googleUser = verifyGoogleToken(request.getAccessToken());
            
            // Find or create user
            SimpleUser user = findOrCreateUserFromGoogle(googleUser);
            
            // Link or update social account
            SocialAccount socialAccount = linkOrUpdateSocialAccount(
                user.getId(), 
                "GOOGLE", 
                googleUser.getId(),
                googleUser.getEmail(),
                googleUser.getName(),
                googleUser.getPicture(),
                request.getAccessToken(),
                null,
                null,
                null
            );
            
            // Generate JWT tokens (simplified - in production, use proper JWT service)
            String accessToken = generateJwtToken(user);
            String refreshToken = generateRefreshToken(user);
            
            SocialLoginResponse response = new SocialLoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setProvider("GOOGLE");
            response.setIsNewUser(socialAccount.getLinkedAt().equals(LocalDateTime.now().withNano(0)));
            response.setIsLinked(true);
            
            return response;
        } catch (Exception e) {
            logger.error("Error authenticating with Google: {}", e.getMessage(), e);
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user with Facebook OAuth2
     */
    @Transactional
    public SocialLoginResponse authenticateWithFacebook(SocialLoginRequest request) {
        logger.info("Authenticating user with Facebook");
        
        try {
            // Verify token and get user info from Facebook
            FacebookUserInfo facebookUser = verifyFacebookToken(request.getAccessToken());
            
            // Find or create user
            SimpleUser user = findOrCreateUserFromFacebook(facebookUser);
            
            // Link or update social account
            SocialAccount socialAccount = linkOrUpdateSocialAccount(
                user.getId(),
                "FACEBOOK",
                facebookUser.getId(),
                facebookUser.getEmail(),
                facebookUser.getName(),
                facebookUser.getPicture() != null ? facebookUser.getPicture().getData().getUrl() : null,
                request.getAccessToken(),
                null,
                null,
                null
            );
            
            // Generate JWT tokens
            String accessToken = generateJwtToken(user);
            String refreshToken = generateRefreshToken(user);
            
            SocialLoginResponse response = new SocialLoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setProvider("FACEBOOK");
            response.setIsNewUser(socialAccount.getLinkedAt().equals(LocalDateTime.now().withNano(0)));
            response.setIsLinked(true);
            
            return response;
        } catch (Exception e) {
            logger.error("Error authenticating with Facebook: {}", e.getMessage(), e);
            throw new RuntimeException("Facebook authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user with Apple Sign-In
     */
    @Transactional
    public SocialLoginResponse authenticateWithApple(SocialLoginRequest request) {
        logger.info("Authenticating user with Apple");
        
        try {
            // Verify ID token and get user info from Apple
            AppleUserInfo appleUser = verifyAppleToken(request.getIdToken());
            
            // Find or create user
            SimpleUser user = findOrCreateUserFromApple(appleUser, request);
            
            // Link or update social account
            SocialAccount socialAccount = linkOrUpdateSocialAccount(
                user.getId(),
                "APPLE",
                appleUser.getSub(),
                appleUser.getEmail(),
                request.getFirstName() != null ? request.getFirstName() + " " + request.getLastName() : null,
                null,
                null,
                request.getIdToken(),
                null,
                null
            );
            
            // Generate JWT tokens
            String accessToken = generateJwtToken(user);
            String refreshToken = generateRefreshToken(user);
            
            SocialLoginResponse response = new SocialLoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setProvider("APPLE");
            response.setIsNewUser(socialAccount.getLinkedAt().equals(LocalDateTime.now().withNano(0)));
            response.setIsLinked(true);
            
            return response;
        } catch (Exception e) {
            logger.error("Error authenticating with Apple: {}", e.getMessage(), e);
            throw new RuntimeException("Apple authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Link social account to existing user
     */
    @Transactional
    public SocialAccount linkSocialAccount(Long userId, LinkSocialAccountRequest request) {
        logger.info("Linking social account {} to user {}", request.getProvider(), userId);
        
        SimpleUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        try {
            SocialAccount socialAccount = null;
            
            switch (request.getProvider().toUpperCase()) {
                case "GOOGLE":
                    GoogleUserInfo googleUser = verifyGoogleToken(request.getAccessToken());
                    socialAccount = linkOrUpdateSocialAccount(
                        userId, "GOOGLE", googleUser.getId(), googleUser.getEmail(),
                        googleUser.getName(), googleUser.getPicture(), request.getAccessToken(), null, null, null
                    );
                    break;
                case "FACEBOOK":
                    FacebookUserInfo facebookUser = verifyFacebookToken(request.getAccessToken());
                    socialAccount = linkOrUpdateSocialAccount(
                        userId, "FACEBOOK", facebookUser.getId(), facebookUser.getEmail(),
                        facebookUser.getName(), 
                        facebookUser.getPicture() != null ? facebookUser.getPicture().getData().getUrl() : null,
                        request.getAccessToken(), null, null, null
                    );
                    break;
                case "APPLE":
                    AppleUserInfo appleUser = verifyAppleToken(request.getIdToken());
                    socialAccount = linkOrUpdateSocialAccount(
                        userId, "APPLE", appleUser.getSub(), appleUser.getEmail(),
                        null, null, null, request.getIdToken(), null, null
                    );
                    break;
                default:
                    throw new RuntimeException("Unsupported provider: " + request.getProvider());
            }
            
            return socialAccount;
        } catch (Exception e) {
            logger.error("Error linking social account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to link social account: " + e.getMessage());
        }
    }
    
    /**
     * Get linked accounts for a user
     */
    public List<LinkedAccountResponse> getLinkedAccounts(Long userId) {
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserIdAndActiveTrue(userId);
        
        return socialAccounts.stream()
            .map(this::toLinkedAccountResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Unlink social account
     */
    @Transactional
    public void unlinkSocialAccount(Long userId, String provider) {
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository.findByUserIdAndProvider(userId, provider);
        
        if (socialAccountOpt.isPresent()) {
            SocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.setActive(false);
            socialAccountRepository.save(socialAccount);
            logger.info("Unlinked social account {} for user {}", provider, userId);
        } else {
            throw new RuntimeException("Social account not found for provider: " + provider);
        }
    }
    
    // Helper methods
    
    private GoogleUserInfo verifyGoogleToken(String accessToken) {
        try {
            String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
            ResponseEntity<GoogleUserInfo> response = getRestTemplate().getForEntity(url, GoogleUserInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to verify Google token");
        } catch (Exception e) {
            logger.error("Error verifying Google token: {}", e.getMessage());
            throw new RuntimeException("Invalid Google token");
        }
    }
    
    private FacebookUserInfo verifyFacebookToken(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            ResponseEntity<FacebookUserInfo> response = getRestTemplate().getForEntity(url, FacebookUserInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to verify Facebook token");
        } catch (Exception e) {
            logger.error("Error verifying Facebook token: {}", e.getMessage());
            throw new RuntimeException("Invalid Facebook token");
        }
    }
    
    private AppleUserInfo verifyAppleToken(String idToken) {
        // In production, verify Apple ID token signature and claims
        // For now, decode JWT and extract claims
        try {
            // Simplified - in production, use proper JWT verification
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid Apple ID token format");
            }
            
            // Decode payload (base64)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            // Parse JSON to get claims
            // For now, return a mock object
            AppleUserInfo appleUser = new AppleUserInfo();
            appleUser.setSub("apple_user_id"); // Extract from token
            appleUser.setEmail("user@example.com"); // Extract from token
            return appleUser;
        } catch (Exception e) {
            logger.error("Error verifying Apple token: {}", e.getMessage());
            throw new RuntimeException("Invalid Apple token");
        }
    }
    
    private SimpleUser findOrCreateUserFromGoogle(GoogleUserInfo googleUser) {
        // Check if social account exists
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
            .findActiveByProviderAndProviderUserId("GOOGLE", googleUser.getId());
        
        if (socialAccountOpt.isPresent()) {
            Long userId = socialAccountOpt.get().getUserId();
            return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        }
        
        // Check if user exists by email
        Optional<SimpleUser> userOpt = userRepository.findByEmail(googleUser.getEmail());
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        // Create new user
        SimpleUser user = new SimpleUser();
        user.setEmail(googleUser.getEmail());
        user.setUsername(googleUser.getEmail().split("@")[0] + "_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
        user.setFirstName(googleUser.getGivenName());
        user.setLastName(googleUser.getFamilyName());
        user.setRoles("USER");
        
        if (TenantContext.hasTenant()) {
            user.setTenantId(TenantContext.getTenantId());
        }
        
        return userRepository.save(user);
    }
    
    private SimpleUser findOrCreateUserFromFacebook(FacebookUserInfo facebookUser) {
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
            .findActiveByProviderAndProviderUserId("FACEBOOK", facebookUser.getId());
        
        if (socialAccountOpt.isPresent()) {
            Long userId = socialAccountOpt.get().getUserId();
            return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        }
        
        Optional<SimpleUser> userOpt = userRepository.findByEmail(facebookUser.getEmail());
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        SimpleUser user = new SimpleUser();
        user.setEmail(facebookUser.getEmail());
        user.setUsername(facebookUser.getEmail().split("@")[0] + "_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        
        String[] nameParts = facebookUser.getName().split(" ", 2);
        user.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            user.setLastName(nameParts[1]);
        }
        user.setRoles("USER");
        
        if (TenantContext.hasTenant()) {
            user.setTenantId(TenantContext.getTenantId());
        }
        
        return userRepository.save(user);
    }
    
    private SimpleUser findOrCreateUserFromApple(AppleUserInfo appleUser, SocialLoginRequest request) {
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
            .findActiveByProviderAndProviderUserId("APPLE", appleUser.getSub());
        
        if (socialAccountOpt.isPresent()) {
            Long userId = socialAccountOpt.get().getUserId();
            return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        }
        
        if (appleUser.getEmail() != null) {
            Optional<SimpleUser> userOpt = userRepository.findByEmail(appleUser.getEmail());
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        
        SimpleUser user = new SimpleUser();
        user.setEmail(appleUser.getEmail() != null ? appleUser.getEmail() : 
            "apple_" + appleUser.getSub() + "@apple.local");
        user.setUsername("apple_" + appleUser.getSub() + "_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoles("USER");
        
        if (TenantContext.hasTenant()) {
            user.setTenantId(TenantContext.getTenantId());
        }
        
        return userRepository.save(user);
    }
    
    private SocialAccount linkOrUpdateSocialAccount(Long userId, String provider, String providerUserId,
                                                   String email, String name, String pictureUrl,
                                                   String accessToken, String idToken, LocalDateTime expiresAt, String refreshToken) {
        Optional<SocialAccount> existingOpt = socialAccountRepository
            .findByProviderAndProviderUserId(provider, providerUserId);
        
        SocialAccount socialAccount;
        if (existingOpt.isPresent()) {
            socialAccount = existingOpt.get();
            socialAccount.setLastUsedAt(LocalDateTime.now());
        } else {
            socialAccount = new SocialAccount();
            socialAccount.setUserId(userId);
            socialAccount.setProvider(provider);
            socialAccount.setProviderUserId(providerUserId);
            socialAccount.setLinkedAt(LocalDateTime.now());
        }
        
        socialAccount.setProviderEmail(email);
        socialAccount.setProviderName(name);
        socialAccount.setPictureUrl(pictureUrl);
        socialAccount.setAccessToken(accessToken);
        socialAccount.setIdToken(idToken);
        socialAccount.setRefreshToken(refreshToken);
        socialAccount.setTokenExpiresAt(expiresAt);
        socialAccount.setActive(true);
        socialAccount.setLastUsedAt(LocalDateTime.now());
        
        if (TenantContext.hasTenant()) {
            socialAccount.setTenantId(TenantContext.getTenantId());
        }
        
        return socialAccountRepository.save(socialAccount);
    }
    
    private LinkedAccountResponse toLinkedAccountResponse(SocialAccount socialAccount) {
        LinkedAccountResponse response = new LinkedAccountResponse();
        response.setId(socialAccount.getId());
        response.setProvider(socialAccount.getProvider());
        response.setProviderEmail(socialAccount.getProviderEmail());
        response.setProviderName(socialAccount.getProviderName());
        response.setPictureUrl(socialAccount.getPictureUrl());
        response.setActive(socialAccount.getActive());
        response.setLinkedAt(socialAccount.getLinkedAt());
        response.setLastUsedAt(socialAccount.getLastUsedAt());
        return response;
    }
    
    private String generateJwtToken(SimpleUser user) {
        // Simplified - in production, use proper JWT service
        return "jwt_token_" + user.getId() + "_" + System.currentTimeMillis();
    }
    
    private String generateRefreshToken(SimpleUser user) {
        // Simplified - in production, use proper JWT service
        return "refresh_token_" + user.getId() + "_" + System.currentTimeMillis();
    }
    
    // Inner classes for provider user info
    static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }
        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
    }
    
    static class FacebookUserInfo {
        private String id;
        private String email;
        private String name;
        private Picture picture;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Picture getPicture() { return picture; }
        public void setPicture(Picture picture) { this.picture = picture; }
        
        private static class Picture {
            private Data data;
            public Data getData() { return data; }
            public void setData(Data data) { this.data = data; }
            
            private static class Data {
                private String url;
                public String getUrl() { return url; }
                public void setUrl(String url) { this.url = url; }
            }
        }
    }
    
    static class AppleUserInfo {
        private String sub;
        private String email;
        
        public String getSub() { return sub; }
        public void setSub(String sub) { this.sub = sub; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}

