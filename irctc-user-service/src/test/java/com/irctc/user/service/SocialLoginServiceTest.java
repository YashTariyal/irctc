package com.irctc.user.service;

import com.irctc.user.dto.LinkSocialAccountRequest;
import com.irctc.user.dto.LinkedAccountResponse;
import com.irctc.user.dto.SocialLoginRequest;
import com.irctc.user.dto.SocialLoginResponse;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.SocialAccount;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.SocialAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {
    
    @Mock
    private SimpleUserRepository userRepository;
    
    @Mock
    private SocialAccountRepository socialAccountRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private SocialLoginService socialLoginService;
    
    private SimpleUser user;
    private SocialAccount socialAccount;
    
    @BeforeEach
    void setUp() {
        user = new SimpleUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("encoded_password");
        user.setRoles("USER");
        
        socialAccount = new SocialAccount();
        socialAccount.setId(1L);
        socialAccount.setUserId(1L);
        socialAccount.setProvider("GOOGLE");
        socialAccount.setProviderUserId("google_user_123");
        socialAccount.setProviderEmail("test@gmail.com");
        socialAccount.setActive(true);
        socialAccount.setLinkedAt(LocalDateTime.now());
    }
    
    @Test
    void testAuthenticateWithGoogle_NewUser() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setAccessToken("google_token");
        request.setProvider("GOOGLE");
        
        // Mock Google API response
        SocialLoginService.GoogleUserInfo googleUser = new SocialLoginService.GoogleUserInfo();
        googleUser.setId("google_user_123");
        googleUser.setEmail("newuser@gmail.com");
        googleUser.setName("New User");
        googleUser.setGivenName("New");
        googleUser.setFamilyName("User");
        googleUser.setPicture("https://example.com/picture.jpg");
        
        ResponseEntity<SocialLoginService.GoogleUserInfo> response = 
            new ResponseEntity<>(googleUser, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(SocialLoginService.GoogleUserInfo.class)))
            .thenReturn(response);
        when(socialAccountRepository.findActiveByProviderAndProviderUserId("GOOGLE", "google_user_123"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@gmail.com"))
            .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(SimpleUser.class))).thenAnswer(invocation -> {
            SimpleUser u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(socialAccountRepository.findByProviderAndProviderUserId("GOOGLE", "google_user_123"))
            .thenReturn(Optional.empty());
        when(socialAccountRepository.save(any(SocialAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        SocialLoginResponse result = socialLoginService.authenticateWithGoogle(request);
        
        assertNotNull(result);
        assertEquals("GOOGLE", result.getProvider());
        assertTrue(result.getIsLinked());
        verify(userRepository, times(1)).save(any(SimpleUser.class));
        verify(socialAccountRepository, times(1)).save(any(SocialAccount.class));
    }
    
    @Test
    void testAuthenticateWithGoogle_ExistingUser() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setAccessToken("google_token");
        request.setProvider("GOOGLE");
        
        SocialLoginService.GoogleUserInfo googleUser = new SocialLoginService.GoogleUserInfo();
        googleUser.setId("google_user_123");
        googleUser.setEmail("test@example.com");
        googleUser.setName("Test User");
        
        ResponseEntity<SocialLoginService.GoogleUserInfo> response = 
            new ResponseEntity<>(googleUser, HttpStatus.OK);
        
        when(restTemplate.getForEntity(anyString(), eq(SocialLoginService.GoogleUserInfo.class)))
            .thenReturn(response);
        when(socialAccountRepository.findActiveByProviderAndProviderUserId("GOOGLE", "google_user_123"))
            .thenReturn(Optional.of(socialAccount));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(socialAccountRepository.findByProviderAndProviderUserId("GOOGLE", "google_user_123"))
            .thenReturn(Optional.of(socialAccount));
        when(socialAccountRepository.save(any(SocialAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        SocialLoginResponse result = socialLoginService.authenticateWithGoogle(request);
        
        assertNotNull(result);
        assertEquals("GOOGLE", result.getProvider());
        assertEquals(1L, result.getUserId());
        verify(socialAccountRepository, times(1)).save(any(SocialAccount.class));
    }
    
    @Test
    void testLinkSocialAccount() {
        LinkSocialAccountRequest request = new LinkSocialAccountRequest();
        request.setAccessToken("google_token");
        request.setProvider("GOOGLE");
        
        SocialLoginService.GoogleUserInfo googleUser = new SocialLoginService.GoogleUserInfo();
        googleUser.setId("google_user_456");
        googleUser.setEmail("test@example.com");
        googleUser.setName("Test User");
        
        ResponseEntity<SocialLoginService.GoogleUserInfo> response = 
            new ResponseEntity<>(googleUser, HttpStatus.OK);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(restTemplate.getForEntity(anyString(), eq(SocialLoginService.GoogleUserInfo.class)))
            .thenReturn(response);
        when(socialAccountRepository.findByProviderAndProviderUserId("GOOGLE", "google_user_456"))
            .thenReturn(Optional.empty());
        when(socialAccountRepository.save(any(SocialAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        SocialAccount result = socialLoginService.linkSocialAccount(1L, request);
        
        assertNotNull(result);
        assertEquals("GOOGLE", result.getProvider());
        assertEquals(1L, result.getUserId());
        verify(socialAccountRepository, times(1)).save(any(SocialAccount.class));
    }
    
    @Test
    void testGetLinkedAccounts() {
        when(socialAccountRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Arrays.asList(socialAccount));
        
        List<LinkedAccountResponse> result = socialLoginService.getLinkedAccounts(1L);
        
        assertEquals(1, result.size());
        assertEquals("GOOGLE", result.get(0).getProvider());
        assertEquals("test@gmail.com", result.get(0).getProviderEmail());
    }
    
    @Test
    void testUnlinkSocialAccount() {
        when(socialAccountRepository.findByUserIdAndProvider(1L, "GOOGLE"))
            .thenReturn(Optional.of(socialAccount));
        when(socialAccountRepository.save(any(SocialAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        socialLoginService.unlinkSocialAccount(1L, "GOOGLE");
        
        assertFalse(socialAccount.getActive());
        verify(socialAccountRepository, times(1)).save(any(SocialAccount.class));
    }
    
    @Test
    void testUnlinkSocialAccount_NotFound() {
        when(socialAccountRepository.findByUserIdAndProvider(1L, "GOOGLE"))
            .thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            socialLoginService.unlinkSocialAccount(1L, "GOOGLE");
        });
    }
}

