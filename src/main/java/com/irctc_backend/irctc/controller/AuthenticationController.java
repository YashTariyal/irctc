package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.LoginRequest;
import com.irctc_backend.irctc.dto.LoginResponse;
import com.irctc_backend.irctc.dto.RefreshTokenRequest;
import com.irctc_backend.irctc.dto.TokenResponse;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * This controller handles authentication-related REST endpoints including
 * user login, token refresh, and authentication status checking.
 * It provides JWT Bearer token authentication for the IRCTC system.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "APIs for user authentication, login, and JWT token management")
public class AuthenticationController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * User login endpoint
     * 
     * @param loginRequest Login request details
     * @return LoginResponse with JWT tokens and user info
     */
    @PostMapping("/login")
    @Operation(
        summary = "🔐 User Login", 
        description = """
            Authenticates a user and returns JWT Bearer tokens for API access.
            
            **Authentication Process:**
            1. Validates username and password
            2. Generates access token (24 hours validity)
            3. Generates refresh token (7 days validity)
            4. Returns user information and tokens
            
            **Token Usage:**
            - Include access token in Authorization header: `Bearer <access_token>`
            - Use refresh token to get new access tokens when expired
            - Access token expires in 24 hours
            - Refresh token expires in 7 days
            
            **Response includes:**
            - Access token for API authentication
            - Refresh token for token renewal
            - Token expiration times
            - User profile information
            """,
        tags = {"🔐 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authenticationService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token endpoint
     * 
     * @param refreshTokenRequest Refresh token request
     * @return TokenResponse with new tokens
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "🔄 Refresh Access Token", 
        description = """
            Refreshes the access token using a valid refresh token.
            
            **Refresh Process:**
            1. Validates the provided refresh token
            2. Generates new access token (24 hours validity)
            3. Generates new refresh token (7 days validity)
            4. Returns new tokens
            
            **When to use:**
            - When access token is expired or about to expire
            - For seamless user experience without re-login
            - Before making API calls with expired tokens
            
            **Security:**
            - Refresh token must be valid and not expired
            - New tokens are generated for security
            - Old refresh token becomes invalid
            """,
        tags = {"🔐 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "401", description = "Refresh token expired or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "Refresh token request", required = true)
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            TokenResponse response = authenticationService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate token endpoint
     * 
     * @param token JWT token to validate
     * @return Token validation result
     */
    @PostMapping("/validate")
    @Operation(
        summary = "✅ Validate Token", 
        description = """
            Validates a JWT token and returns its status.
            
            **Validation Process:**
            1. Checks token format and signature
            2. Verifies token expiration
            3. Returns validation result
            
            **Use Cases:**
            - Check if token is still valid before API calls
            - Validate tokens from frontend applications
            - Debug authentication issues
            
            **Response:**
            - Valid: Token is valid and not expired
            - Invalid: Token is malformed, expired, or tampered with
            """,
        tags = {"🔐 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation result"),
        @ApiResponse(responseCode = "400", description = "Invalid token format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "JWT token to validate", required = true)
            @RequestParam String token) {
        try {
            boolean isValid = authenticationService.validateToken(token);
            return ResponseEntity.ok(new TokenValidationResponse(isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Token validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Get current user info endpoint
     * 
     * @return Current authenticated user information
     */
    @GetMapping("/me")
    @Operation(
        summary = "👤 Get Current User", 
        description = """
            Returns information about the currently authenticated user.
            
            **Authentication Required:**
            - Valid Bearer token in Authorization header
            - Token must not be expired
            
            **Response includes:**
            - User ID, username, email
            - User role and permissions
            - Account status (active, verified)
            - Profile information
            
            **Use Cases:**
            - Get current user context in frontend
            - Display user profile information
            - Check user permissions and roles
            """,
        tags = {"🔐 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCurrentUser() {
        try {
            return authenticationService.getCurrentUser()
                .map(user -> ResponseEntity.ok(new UserInfoResponse(user)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found or not authenticated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user information: " + e.getMessage());
        }
    }
    
    /**
     * Logout endpoint (token invalidation)
     * 
     * @return Logout confirmation
     */
    @PostMapping("/logout")
    @Operation(
        summary = "🚪 User Logout", 
        description = """
            Logs out the current user and invalidates the session.
            
            **Logout Process:**
            1. Clears the security context
            2. Invalidates the current session
            3. Returns logout confirmation
            
            **Note:**
            - JWT tokens are stateless and cannot be invalidated server-side
            - Client should discard tokens after logout
            - For enhanced security, implement token blacklisting
            
            **Security Best Practices:**
            - Clear tokens from client storage
            - Redirect to login page
            - Clear any cached user data
            """,
        tags = {"🔐 Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> logout() {
        try {
            // Clear security context
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(new LogoutResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Logout failed: " + e.getMessage());
        }
    }
    
    /**
     * Token validation response class
     */
    private static class TokenValidationResponse {
        public final boolean valid;
        public final String message;
        
        public TokenValidationResponse(boolean valid) {
            this.valid = valid;
            this.message = valid ? "Token is valid" : "Token is invalid or expired";
        }
    }
    
    /**
     * User info response class
     */
    private static class UserInfoResponse {
        public final Long id;
        public final String username;
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String role;
        public final boolean isActive;
        public final boolean isVerified;
        
        public UserInfoResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole().name();
            this.isActive = user.getIsActive();
            this.isVerified = user.getIsVerified();
        }
    }
    
    /**
     * Logout response class
     */
    private static class LogoutResponse {
        public final boolean success = true;
        public final String message = "Logout successful";
        public final String timestamp = java.time.LocalDateTime.now().toString();
    }
}
