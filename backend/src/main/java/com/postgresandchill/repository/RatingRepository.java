package com.postgresandchill.repository;

import com.postgresandchill.model.Rating;
import com.postgresandchill.model.Rating.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    @Query("SELECT r FROM Rating r WHERE r.id.userId = :userId")
    List<Rating> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT r FROM Rating r WHERE r.id.userId = :userId AND r.rating >= 3.5")
    List<Rating> findLikedByUser(@Param("userId") Integer userId);

    // Users who liked at least one of the given movies (excluding the target user)
    @Query("""
        SELECT DISTINCT r.id.userId
        FROM Rating r
        WHERE r.id.movieId IN :movieIds
          AND r.rating >= 3.5
          AND r.id.userId <> :userId
    """)
    List<Integer> findSimilarUsers(
            @Param("movieIds") List<Integer> movieIds,
            @Param("userId") Integer excludeUserId
    );

    // All ratings for a set of users (used for similarity + scoring)
    @Query("""
        SELECT r
        FROM Rating r
        WHERE r.id.userId IN :userIds
    """)
    List<Rating> findRatingsForUsers(@Param("userIds") List<Integer> userIds);

    // Global average rating (for Bayesian adjustment)
    @Query("SELECT AVG(r.rating) FROM Rating r")
    Double findGlobalAverageRating();
}
