package com.irctc.user.dto;

import lombok.Data;

/**
 * DTO for social login request
 */
@Data
public class SocialLoginRequest {
    private String accessToken; // OAuth2 access token from provider
    private String idToken; // ID token (for Apple Sign-In)
    private String provider; // GOOGLE, FACEBOOK, APPLE
    private String email; // Optional: email from provider
    private String firstName; // Optional: first name from provider
    private String lastName; // Optional: last name from provider
}

