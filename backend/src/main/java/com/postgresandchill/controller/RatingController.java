package com.postgresandchill.controller;

import com.postgresandchill.model.Rating;
import com.postgresandchill.model.Rating.RatingId;
import com.postgresandchill.repository.RatingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {

    private final RatingRepository ratingRepository;

    public RatingController(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    //  Get specific rating
    @GetMapping("/{userId}/{movieId}")
    public Optional<Rating> getRating(
            @PathVariable Integer userId,
            @PathVariable Integer movieId
    ) {
        RatingId id = new RatingId(userId, movieId);
        return ratingRepository.findById(id);
    }

    //  Get all ratings for a user
    @GetMapping("/user/{userId}")
    public List<Rating> getRatingsForUser(@PathVariable Integer userId) {
        return ratingRepository.findByUserId(userId);
    }

    //  Clear rating
    @DeleteMapping("/{userId}/{movieId}")
    public void deleteRating(
            @PathVariable Integer userId,
            @PathVariable Integer movieId
    ) {
        RatingId id = new RatingId(userId, movieId);
        ratingRepository.deleteById(id);
    }

    //  Save or update rating — matches payload used by dashboard
    //
    // Expected JSON:
    // {
    //   "id": { "userId": 1, "movieId": 1 },
    //   "rating": 4
    // }
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Rating saveRating(@RequestBody Map<String, Object> payload) {

        if (payload == null || !payload.containsKey("id")) {
            throw new IllegalArgumentException("Body must contain 'id' with 'userId' and 'movieId'");
        }

        Object idObj = payload.get("id");
        if (!(idObj instanceof Map)) {
            throw new IllegalArgumentException("'id' must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> idMap = (Map<String, Object>) idObj;

        Object userIdObj = idMap.get("userId");
        Object movieIdObj = idMap.get("movieId");

        if (!(userIdObj instanceof Number) || !(movieIdObj instanceof Number)) {
            throw new IllegalArgumentException("'userId' and 'movieId' must be numeric");
        }

        Integer userId = ((Number) userIdObj).intValue();
        Integer movieId = ((Number) movieIdObj).intValue();

        Number ratingNum = payload.get("rating") instanceof Number
                ? (Number) payload.get("rating")
                : null;

        Float ratingValue = ratingNum != null ? ratingNum.floatValue() : null;

        RatingId id = new RatingId(userId, movieId);

        // Upsert: if rating exists, update it; otherwise create new
        Rating rating = ratingRepository.findById(id).orElseGet(() -> {
            Rating r = new Rating();
            r.setId(id);
            return r;
        });

        rating.setRating(ratingValue);
        rating.setTimestamp(System.currentTimeMillis());

        return ratingRepository.save(rating);
    }
}
