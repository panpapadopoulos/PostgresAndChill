package com.postgresandchill.controller;

import com.postgresandchill.dto.RecommendedMovieDTO;
import com.postgresandchill.dto.RecommendationRequest;
import com.postgresandchill.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<RecommendedMovieDTO> recommend(@RequestBody RecommendationRequest request) {
        return recommendationService.getRecommendationsDetailed(
                request.getUserId(),
                request.getLimit()
        );
    }
}
