package com.postgresandchill.dto;

public class RecommendationRequest {

    private Long userId;
    private int limit = 5;

    public RecommendationRequest() {
    }

    public RecommendationRequest(Long userId, int limit) {
        this.userId = userId;
        this.limit = limit;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
