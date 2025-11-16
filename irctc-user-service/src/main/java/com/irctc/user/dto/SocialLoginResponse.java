package com.irctc.user.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for social login response
 */
@Data
public class SocialLoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String accessToken; // JWT access token
    private String refreshToken; // JWT refresh token
    private String provider; // GOOGLE, FACEBOOK, APPLE
    private Boolean isNewUser; // Whether this is a newly created user
    private Boolean isLinked; // Whether account was linked to existing user
}

