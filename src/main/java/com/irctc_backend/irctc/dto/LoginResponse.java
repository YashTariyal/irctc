package com.irctc_backend.irctc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Login Response DTO
 * 
 * This DTO represents the login response data structure containing
 * JWT tokens and user information after successful authentication.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    
    private Long refreshExpiresIn;
    
    private UserInfo user;
    
    private LocalDateTime loginTime;
    
    /**
     * User information nested class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private boolean isActive;
        private boolean isVerified;
    }
    
    /**
     * Create successful login response
     * 
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @param expiresIn Access token expiration time
     * @param refreshExpiresIn Refresh token expiration time
     * @param user User information
     * @return LoginResponse instance
     */
    public static LoginResponse success(String accessToken, String refreshToken, 
                                      Long expiresIn, Long refreshExpiresIn, 
                                      UserInfo user) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setRefreshExpiresIn(refreshExpiresIn);
        response.setUser(user);
        response.setLoginTime(LocalDateTime.now());
        return response;
    }
}
