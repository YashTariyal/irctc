package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import com.irctc_backend.irctc.dto.LoginRequest;
import com.irctc_backend.irctc.dto.LoginResponse;
import com.irctc_backend.irctc.dto.RefreshTokenRequest;
import com.irctc_backend.irctc.dto.TokenResponse;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.repository.UserRepository;
import com.irctc_backend.irctc.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service
 * 
 * This service handles user authentication, JWT token generation,
 * and token refresh operations for the IRCTC application.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class AuthenticationService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Authenticate user and generate JWT tokens
     * 
     * @param loginRequest Login request details
     * @return LoginResponse with tokens and user info
     */
    @ExecutionTime("User Authentication")
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            logger.info("Authenticating user: {}", loginRequest.getUsername());
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            // Generate tokens
            String accessToken = jwtUtil.generateTokenWithUserDetails(
                user.getUsername(), 
                user.getId(), 
                user.getRole().name()
            );
            
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            // Create user info
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setFirstName(user.getFirstName());
            userInfo.setLastName(user.getLastName());
            userInfo.setRole(user.getRole().name());
            userInfo.setActive(user.getIsActive());
            userInfo.setVerified(user.getIsVerified());
            
            logger.info("User '{}' authenticated successfully", user.getUsername());
            
            return LoginResponse.success(
                accessToken,
                refreshToken,
                jwtUtil.getExpirationTime(),
                jwtUtil.getRefreshExpirationTime(),
                userInfo
            );
            
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new RuntimeException("Invalid username or password");
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshTokenRequest Refresh token request
     * @return TokenResponse with new tokens
     */
    @ExecutionTime("Token Refresh")
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            
            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            
            // Get user details
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            // Generate new tokens
            String newAccessToken = jwtUtil.generateTokenWithUserDetails(
                user.getUsername(), 
                user.getId(), 
                user.getRole().name()
            );
            
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            logger.info("Tokens refreshed successfully for user: {}", username);
            
            return TokenResponse.create(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getExpirationTime(),
                jwtUtil.getRefreshExpirationTime()
            );
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user details by username
     * 
     * @param username Username
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getRole().name())
            .accountExpired(false)
            .accountLocked(!user.getIsActive())
            .credentialsExpired(false)
            .disabled(!user.getIsActive())
            .build();
    }
    
    /**
     * Get current user from security context
     * 
     * @return Current user
     */
    public Optional<User> getCurrentUser() {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
            
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get current user ID from security context
     * 
     * @return Current user ID
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }
    
    /**
     * Check if current user has specific role
     * 
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        try {
            return org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
        } catch (Exception e) {
            logger.error("Error checking user role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if current user is admin
     * 
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
