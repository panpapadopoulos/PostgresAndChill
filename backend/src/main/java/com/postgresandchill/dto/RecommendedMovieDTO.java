package com.postgresandchill.dto;

import java.util.List;

public class RecommendedMovieDTO {

    private Long movieId;
    private String title;
    private Integer releaseYear;
    private List<String> genres;

    private double score;      // final hybrid score
    private List<String> reasons; // explanation list

    public RecommendedMovieDTO() {}

    public RecommendedMovieDTO(Long movieId,
                               String title,
                               Integer releaseYear,
                               List<String> genres,
                               double score,
                               List<String> reasons) {
        this.movieId = movieId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.score = score;
        this.reasons = reasons;
    }

    public Long getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public Integer getReleaseYear() { return releaseYear; }
    public List<String> getGenres() { return genres; }
    public double getScore() { return score; }
    public List<String> getReasons() { return reasons; }

    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public void setTitle(String title) { this.title = title; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public void setScore(double score) { this.score = score; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
