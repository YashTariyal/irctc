package com.irctc.user.dto;

import lombok.Data;

/**
 * DTO for linking social account to existing user
 */
@Data
public class LinkSocialAccountRequest {
    private String accessToken; // OAuth2 access token from provider
    private String idToken; // ID token (for Apple Sign-In)
    private String provider; // GOOGLE, FACEBOOK, APPLE
}

