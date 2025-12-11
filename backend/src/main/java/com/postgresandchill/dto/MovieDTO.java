package com.postgresandchill.dto;

import java.util.List;

public class MovieDTO {
    private Integer movieId;
    private String title;
    private Integer releaseYear;
    private Float averageRating;
    private List<String> genres;

    public MovieDTO(Integer movieId, String title, Integer releaseYear, Float averageRating, List<String> genres) {
        this.movieId = movieId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.genres = genres;
    }

    public Integer getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public Integer getReleaseYear() { return releaseYear; }
    public Float getAverageRating() { return averageRating; }
    public List<String> getGenres() { return genres; }
}
