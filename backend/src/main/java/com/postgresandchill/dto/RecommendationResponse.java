package com.postgresandchill.dto;

import java.util.List;

public class RecommendationResponse {

    private List<Long> movieIds;

    public RecommendationResponse() {
    }

    public RecommendationResponse(List<Long> movieIds) {
        this.movieIds = movieIds;
    }

    public List<Long> getMovieIds() {
        return movieIds;
    }

    public void setMovieIds(List<Long> movieIds) {
        this.movieIds = movieIds;
    }
}
