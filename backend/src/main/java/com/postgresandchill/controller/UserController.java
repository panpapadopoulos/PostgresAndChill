package com.postgresandchill.controller;

import com.postgresandchill.model.Rating;
import com.postgresandchill.model.Rating.RatingId;
import com.postgresandchill.model.User;
import com.postgresandchill.repository.RatingRepository;
import com.postgresandchill.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public UserController(UserRepository userRepository, RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{username}")
    public Optional<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }

    // ✔ Assigns next max id manually
    @PostMapping
    public User createUser(@RequestBody User user) {
        int nextId = userRepository.getMaxUserId() + 1;
        user.setUserId(nextId);
        return userRepository.save(user);
    }

    // ⭐ NEW: Cold-start rating batch submit
    @PostMapping("/{userId}/ratings")
    public String saveInitialRatings(
            @PathVariable Integer userId,
            @RequestBody Map<Integer, Integer> ratings) {

        long now = System.currentTimeMillis();

        ratings.forEach((movieId, ratingValue) -> {
            Rating rating = new Rating();
            RatingId id = new RatingId();
            id.setUserId(userId);
            id.setMovieId(movieId);

            rating.setId(id);
            rating.setRating(ratingValue);
            rating.setTimestamp(now);

            ratingRepository.save(rating);
        });

        return "OK";
    }
}
