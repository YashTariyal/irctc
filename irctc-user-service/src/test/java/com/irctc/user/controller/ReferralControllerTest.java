package com.irctc.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.user.dto.ReferralCodeResponse;
import com.irctc.user.dto.ReferralLeaderboardEntry;
import com.irctc.user.dto.ReferralRewardRequest;
import com.irctc.user.dto.ReferralSummaryResponse;
import com.irctc.user.service.ReferralService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferralControllerTest {

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private ReferralController referralController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(referralController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
            .build();
    }

    @Test
    void shouldReturnReferralCode() throws Exception {
        ReferralCodeResponse response = new ReferralCodeResponse();
        response.setUserId(1L);
        response.setReferralCode("RF123");
        response.setTotalPoints(100);
        response.setTotalReferrals(5L);
        when(referralService.getReferralCode(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1/referral-code"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.referralCode").value("RF123"));
    }

    @Test
    void shouldReturnLeaderboard() throws Exception {
        ReferralLeaderboardEntry entry = new ReferralLeaderboardEntry();
        entry.setUserId(1L);
        entry.setUsername("alice");
        entry.setReferralPoints(200);
        entry.setRank(1);
        when(referralService.getLeaderboard(5)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/users/referral-leaderboard").param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void shouldAcceptRewardRequest() throws Exception {
        doNothing().when(referralService).recordBookingReward(any(ReferralRewardRequest.class));
        ReferralRewardRequest request = new ReferralRewardRequest();
        request.setReferredUserId(2L);

        mockMvc.perform(post("/api/users/referrals/reward")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturnReferralSummary() throws Exception {
        ReferralSummaryResponse summary = new ReferralSummaryResponse();
        summary.setUserId(1L);
        summary.setTotalPoints(100);
        summary.setTotalReferrals(2);
        when(referralService.getReferralSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/users/1/referrals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L));
    }
}

