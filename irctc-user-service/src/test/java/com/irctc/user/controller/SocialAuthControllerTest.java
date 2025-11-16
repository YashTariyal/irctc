package com.irctc.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.user.dto.LinkSocialAccountRequest;
import com.irctc.user.dto.LinkedAccountResponse;
import com.irctc.user.dto.SocialLoginRequest;
import com.irctc.user.dto.SocialLoginResponse;
import com.irctc.user.entity.SocialAccount;
import com.irctc.user.service.SocialLoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SocialAuthControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private SocialLoginService socialLoginService;
    
    @InjectMocks
    private SocialAuthController socialAuthController;
    
    private ObjectMapper objectMapper;
    
    private SocialLoginResponse socialLoginResponse;
    private LinkedAccountResponse linkedAccountResponse;
    private SocialAccount socialAccount;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(socialAuthController)
                .setMessageConverters(converter)
                .build();
        
        socialLoginResponse = new SocialLoginResponse();
        socialLoginResponse.setUserId(1L);
        socialLoginResponse.setUsername("testuser");
        socialLoginResponse.setEmail("test@example.com");
        socialLoginResponse.setFirstName("Test");
        socialLoginResponse.setLastName("User");
        socialLoginResponse.setAccessToken("jwt_token");
        socialLoginResponse.setRefreshToken("refresh_token");
        socialLoginResponse.setProvider("GOOGLE");
        socialLoginResponse.setIsNewUser(true);
        socialLoginResponse.setIsLinked(true);
        
        linkedAccountResponse = new LinkedAccountResponse();
        linkedAccountResponse.setId(1L);
        linkedAccountResponse.setProvider("GOOGLE");
        linkedAccountResponse.setProviderEmail("test@gmail.com");
        linkedAccountResponse.setActive(true);
        linkedAccountResponse.setLinkedAt(LocalDateTime.now());
        
        socialAccount = new SocialAccount();
        socialAccount.setId(1L);
        socialAccount.setUserId(1L);
        socialAccount.setProvider("GOOGLE");
    }
    
    @Test
    void testAuthenticateWithGoogle() throws Exception {
        when(socialLoginService.authenticateWithGoogle(any(SocialLoginRequest.class)))
            .thenReturn(socialLoginResponse);
        
        mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"google_token\",\"provider\":\"GOOGLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.isLinked").value(true));
    }
    
    @Test
    void testAuthenticateWithFacebook() throws Exception {
        socialLoginResponse.setProvider("FACEBOOK");
        when(socialLoginService.authenticateWithFacebook(any(SocialLoginRequest.class)))
            .thenReturn(socialLoginResponse);
        
        mockMvc.perform(post("/api/auth/facebook")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"facebook_token\",\"provider\":\"FACEBOOK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("FACEBOOK"));
    }
    
    @Test
    void testAuthenticateWithApple() throws Exception {
        socialLoginResponse.setProvider("APPLE");
        when(socialLoginService.authenticateWithApple(any(SocialLoginRequest.class)))
            .thenReturn(socialLoginResponse);
        
        mockMvc.perform(post("/api/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"apple_token\",\"provider\":\"APPLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("APPLE"));
    }
    
    @Test
    void testLinkSocialAccount() throws Exception {
        when(socialLoginService.linkSocialAccount(eq(1L), any(LinkSocialAccountRequest.class)))
            .thenReturn(socialAccount);
        
        mockMvc.perform(post("/api/auth/users/1/link-social-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"token\",\"provider\":\"GOOGLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("GOOGLE"));
    }
    
    @Test
    void testGetLinkedAccounts() throws Exception {
        when(socialLoginService.getLinkedAccounts(1L))
            .thenReturn(Arrays.asList(linkedAccountResponse));
        
        mockMvc.perform(get("/api/auth/users/1/linked-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("GOOGLE"))
                .andExpect(jsonPath("$[0].providerEmail").value("test@gmail.com"));
    }
    
    @Test
    void testUnlinkSocialAccount() throws Exception {
        mockMvc.perform(delete("/api/auth/users/1/linked-accounts/GOOGLE"))
                .andExpect(status().isNoContent());
    }
}

