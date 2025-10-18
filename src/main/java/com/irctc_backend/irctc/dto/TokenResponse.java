package com.irctc_backend.irctc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token Response DTO
 * 
 * This DTO represents the token response data structure for
 * token refresh operations.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    
    private Long refreshExpiresIn;
    
    /**
     * Create token response
     * 
     * @param accessToken New access token
     * @param refreshToken New refresh token
     * @param expiresIn Access token expiration time
     * @param refreshExpiresIn Refresh token expiration time
     * @return TokenResponse instance
     */
    public static TokenResponse create(String accessToken, String refreshToken, 
                                     Long expiresIn, Long refreshExpiresIn) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setRefreshExpiresIn(refreshExpiresIn);
        return response;
    }
}
