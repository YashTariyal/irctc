package com.irctc.train.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.train.dto.RecommendationRequest;
import com.irctc.train.dto.RecommendationResponse;
import com.irctc.train.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private MockMvc mockMvc;
    private RecommendationService recommendationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        recommendationService = Mockito.mock(RecommendationService.class);
        RecommendationController controller = new RecommendationController(recommendationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldReturnRecommendations() throws Exception {
        RecommendationResponse response = new RecommendationResponse();
        response.setTrainNumber("12345");
        response.setAvailabilityScore(95.0);
        when(recommendationService.recommendTrains(any(RecommendationRequest.class)))
            .thenReturn(List.of(response));

        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(10L);
        request.setSourceStation("NDLS");
        request.setDestinationStation("BCT");

        mockMvc.perform(post("/api/trains/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].trainNumber").value("12345"));
    }
}

