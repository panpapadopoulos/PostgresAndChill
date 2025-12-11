package com.postgresandchill.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ratings")
public class Rating {

    @EmbeddedId
    private RatingId id;

    private Float rating;
    private Long timestamp;

    @Embeddable
    public static class RatingId implements Serializable {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "movie_id")
        private Integer movieId;

        public RatingId() {}

        public RatingId(Integer userId, Integer movieId) {
            this.userId = userId;
            this.movieId = movieId;
        }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public Integer getMovieId() { return movieId; }
        public void setMovieId(Integer movieId) { this.movieId = movieId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RatingId)) return false;
            RatingId that = (RatingId) o;
            return Objects.equals(userId, that.userId) &&
                   Objects.equals(movieId, that.movieId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, movieId);
        }
    }

    public RatingId getId() { return id; }
    public void setId(RatingId id) { this.id = id; }

    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }

    public void setRating(Number rating) {
        if (rating != null) this.rating = rating.floatValue();
    }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
