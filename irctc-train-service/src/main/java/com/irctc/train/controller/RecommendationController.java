package com.irctc.train.controller;

import com.irctc.train.dto.RecommendationRequest;
import com.irctc.train.dto.RecommendationResponse;
import com.irctc.train.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<List<RecommendationResponse>> recommendTrains(@RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.recommendTrains(request));
    }
}

