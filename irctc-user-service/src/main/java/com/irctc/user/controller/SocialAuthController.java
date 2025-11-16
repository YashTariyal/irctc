package com.irctc.user.controller;

import com.irctc.user.dto.LinkSocialAccountRequest;
import com.irctc.user.dto.LinkedAccountResponse;
import com.irctc.user.dto.SocialLoginRequest;
import com.irctc.user.dto.SocialLoginResponse;
import com.irctc.user.entity.SocialAccount;
import com.irctc.user.service.SocialLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for social login authentication (Google, Facebook, Apple)
 */
@RestController
@RequestMapping("/api/auth")
public class SocialAuthController {
    
    @Autowired
    private SocialLoginService socialLoginService;
    
    /**
     * POST /api/auth/google
     * Authenticate user with Google OAuth2
     */
    @PostMapping("/google")
    public ResponseEntity<SocialLoginResponse> authenticateWithGoogle(
            @RequestBody SocialLoginRequest request) {
        request.setProvider("GOOGLE");
        SocialLoginResponse response = socialLoginService.authenticateWithGoogle(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/facebook
     * Authenticate user with Facebook OAuth2
     */
    @PostMapping("/facebook")
    public ResponseEntity<SocialLoginResponse> authenticateWithFacebook(
            @RequestBody SocialLoginRequest request) {
        request.setProvider("FACEBOOK");
        SocialLoginResponse response = socialLoginService.authenticateWithFacebook(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/apple
     * Authenticate user with Apple Sign-In
     */
    @PostMapping("/apple")
    public ResponseEntity<SocialLoginResponse> authenticateWithApple(
            @RequestBody SocialLoginRequest request) {
        request.setProvider("APPLE");
        SocialLoginResponse response = socialLoginService.authenticateWithApple(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/users/{id}/link-social-account
     * Link social account to existing user
     */
    @PostMapping("/users/{id}/link-social-account")
    public ResponseEntity<SocialAccount> linkSocialAccount(
            @PathVariable Long id,
            @RequestBody LinkSocialAccountRequest request) {
        SocialAccount socialAccount = socialLoginService.linkSocialAccount(id, request);
        return ResponseEntity.ok(socialAccount);
    }
    
    /**
     * GET /api/users/{id}/linked-accounts
     * Get all linked social accounts for a user
     */
    @GetMapping("/users/{id}/linked-accounts")
    public ResponseEntity<List<LinkedAccountResponse>> getLinkedAccounts(@PathVariable Long id) {
        List<LinkedAccountResponse> linkedAccounts = socialLoginService.getLinkedAccounts(id);
        return ResponseEntity.ok(linkedAccounts);
    }
    
    /**
     * DELETE /api/users/{id}/linked-accounts/{provider}
     * Unlink social account
     */
    @DeleteMapping("/users/{id}/linked-accounts/{provider}")
    public ResponseEntity<Void> unlinkSocialAccount(
            @PathVariable Long id,
            @PathVariable String provider) {
        socialLoginService.unlinkSocialAccount(id, provider);
        return ResponseEntity.noContent().build();
    }
}

