package com.postgresandchill.service;

import com.postgresandchill.dto.RecommendedMovieDTO;
import com.postgresandchill.model.Movie;
import com.postgresandchill.model.Rating;
import com.postgresandchill.repository.MovieRepository;
import com.postgresandchill.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;

    @Autowired
    public RecommendationService(RatingRepository ratingRepository,
                                 MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
    }

    // =============================================================
    //   MAIN ENTRY POINT FOR CONTROLLER (FULL MOVIE OBJECTS)
    // =============================================================
    public List<RecommendedMovieDTO> getRecommendationsDetailed(Long userId, int limit) {

        List<Long> orderedMovieIds = getRecommendations(userId, limit);

        if (orderedMovieIds.isEmpty()) return List.of();

        Iterable<Movie> movies = movieRepository.findAllById(
                orderedMovieIds.stream().map(Long::intValue).toList()
        );

        Map<Integer, Movie> movieMap = new HashMap<>();
        for (Movie m : movies) {
            movieMap.put(m.getMovieId(), m);
        }

        List<RecommendedMovieDTO> result = new ArrayList<>();

        for (Long id : orderedMovieIds) {
            Movie m = movieMap.get(id.intValue());
            if (m == null) continue;

            List<String> reasons = generateReasons(m, userId);

            List<String> genres = (m.getGenres() != null)
                    ? m.getGenres().stream().map(g -> g.getName()).toList()
                    : List.of();

            result.add(new RecommendedMovieDTO(
                    m.getMovieId().longValue(),
                    m.getTitle(),
                    m.getReleaseYear(),
                    genres,
                    0.0,          // optionally fill in hybrid score later
                    reasons
            ));
        }

        return result;
    }

    // =============================================================
    //   HYBRID ALGORITHM — RETURNS ONLY MOVIE IDs IN ORDER
    // =============================================================
    public List<Long> getRecommendations(Long userId, int limit) {

        int effectiveLimit = (limit <= 0 || limit > 50) ? 10 : limit;
        Integer uid = userId.intValue();

        // 1) fetch user likes
        List<Rating> userLikes = ratingRepository.findLikedByUser(uid);
        if (userLikes.isEmpty()) {
            return fallbackGlobalRecommendations(uid, effectiveLimit);
        }

        Set<Integer> userLikedMovies = userLikes.stream()
                .map(r -> r.getId().getMovieId())
                .collect(Collectors.toSet());

        // 2) find similar users
        List<Integer> similarUsers = ratingRepository.findSimilarUsers(
                new ArrayList<>(userLikedMovies), uid
        );
        if (similarUsers.isEmpty()) {
            return fallbackGlobalRecommendations(uid, effectiveLimit);
        }

        // fetch all their ratings
        List<Rating> similarUserRatings =
                ratingRepository.findRatingsForUsers(similarUsers);

        // 3) compute similarity per user
        Map<Integer, Double> userSimilarity = computeUserSimilarities(
                userLikedMovies, similarUserRatings
        );

        if (userSimilarity.isEmpty()) {
            return fallbackGlobalRecommendations(uid, effectiveLimit);
        }

        // 4) movies the target user already saw
        Set<Integer> userSeenMovies = ratingRepository.findByUserId(uid).stream()
                .map(r -> r.getId().getMovieId())
                .collect(Collectors.toSet());

        // 5) CF movie scores
        Map<Integer, Double> cfScores = computeCollaborativeMovieScores(
                userSimilarity, similarUserRatings, userSeenMovies
        );

        if (cfScores.isEmpty()) {
            return fallbackGlobalRecommendations(uid, effectiveLimit);
        }

        List<Integer> candidateIds = new ArrayList<>(cfScores.keySet());

        // 6) fetch stats for candidates
        List<Object[]> rows = movieRepository.findDetailsForMovieIds(candidateIds);

        // 7) user genre profile
        Map<String, Double> userGenreWeights = buildUserGenreWeights(userLikedMovies);

        // 8) global rating average for Bayesian smoothing
        Double global = ratingRepository.findGlobalAverageRating();
        double globalAvg = (global != null) ? global : 3.5;
        double bayesM = 20.0;

        Map<Integer, Double> bayesMap = new HashMap<>();
        Map<Integer, Double> genreMap = new HashMap<>();
        Map<Integer, Double> popMap = new HashMap<>();
        Map<Integer, String> primaryGenreMap = new HashMap<>();

        double maxCf = cfScores.values().stream().mapToDouble(v -> v).max().orElse(1.0);
        double maxBayes = 0, maxGenre = 0, maxPop = 0;

        for (Object[] row : rows) {
            Integer movieId = (Integer) row[0];
            Double avg = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;
            long ratingCount = (row[4] != null) ? ((Number) row[4]).longValue() : 0L;
            String genresCsv = (String) row[5];

            Set<String> movieGenres = parseGenresCsv(genresCsv);

            double bayesScore = computeBayesianScore(avg, ratingCount, globalAvg, bayesM);
            double popScore = (ratingCount > 0) ? Math.log(1 + ratingCount) : 0;
            double genreScore = computeGenreScore(movieGenres, userGenreWeights);

            bayesMap.put(movieId, bayesScore);
            genreMap.put(movieId, genreScore);
            popMap.put(movieId, popScore);

            primaryGenreMap.put(movieId,
                    movieGenres.stream().findFirst().orElse("unknown"));

            maxBayes = Math.max(maxBayes, bayesScore);
            maxGenre = Math.max(maxGenre, genreScore);
            maxPop = Math.max(maxPop, popScore);
        }

        // 9) combine hybrid scores
        Map<Integer, Double> finalScores = new HashMap<>();
        for (Integer id : candidateIds) {

            double cfNorm = cfScores.get(id) / maxCf;

            double bayesNorm = (maxBayes > 0) ? bayesMap.get(id) / maxBayes : 0;
            double genreNorm = (maxGenre > 0) ? genreMap.get(id) / maxGenre : 0;
            double popNorm = (maxPop > 0) ? popMap.get(id) / maxPop : 0;

            double scored =
                    0.4 * cfNorm +
                    0.3 * bayesNorm +
                    0.2 * genreNorm +
                    0.1 * popNorm;

            finalScores.put(id, scored);
        }

        // 10) pick with diversity
        return pickWithDiversity(finalScores, primaryGenreMap, effectiveLimit);
    }

    // =============================================================
    //   SIMILARITY
    // =============================================================
    private Map<Integer, Double> computeUserSimilarities(
            Set<Integer> targetLikedMovies,
            List<Rating> ratings
    ) {

        Map<Integer, List<Rating>> byUser = ratings.stream()
                .collect(Collectors.groupingBy(r -> r.getId().getUserId()));

        Map<Integer, Double> sim = new HashMap<>();

        for (var entry : byUser.entrySet()) {
            Integer userId = entry.getKey();
            List<Rating> userRatings = entry.getValue();

            double score = 0;

            for (Rating r : userRatings) {
                if (targetLikedMovies.contains(r.getId().getMovieId())) {
                    double rs = Math.max(0, r.getRating() - 3.5);
                    double rec = computeRecencyWeight(r.getTimestamp());
                    score += rs * rec;
                }
            }

            if (score > 0) sim.put(userId, score);
        }

        return sim;
    }

    private double computeRecencyWeight(Long timestamp) {
        if (timestamp == null) return 1.0;

        long now = System.currentTimeMillis() / 1000;
        long ageSec = now - timestamp;
        double months = ageSec / (60.0 * 60 * 24 * 30);

        return 1.0 / (1.0 + months / 12.0);
    }

    // =============================================================
    //   COLLABORATIVE SCORE
    // =============================================================
    private Map<Integer, Double> computeCollaborativeMovieScores(
            Map<Integer, Double> userSim,
            List<Rating> ratings,
            Set<Integer> userSeen
    ) {
        Map<Integer, Double> scores = new HashMap<>();

        for (Rating r : ratings) {
            Integer movieId = r.getId().getMovieId();
            Integer uid = r.getId().getUserId();

            if (userSeen.contains(movieId)) continue;
            if (!userSim.containsKey(uid)) continue;

            double simW = userSim.get(uid);
            double ratingStrength = Math.max(0, r.getRating() - 3.5);
            double rec = computeRecencyWeight(r.getTimestamp());

            double add = simW * ratingStrength * rec;
            scores.put(movieId, scores.getOrDefault(movieId, 0.0) + add);
        }

        return scores;
    }

    // =============================================================
    //   CONTENT BOOSTS
    // =============================================================
    private Map<String, Double> buildUserGenreWeights(Set<Integer> likedMovies) {

        Map<String, Integer> freq = new HashMap<>();

        Iterable<Movie> movies = movieRepository.findAllById(likedMovies);
        for (Movie m : movies) {
            if (m.getGenres() == null) continue;
            m.getGenres().forEach(g -> {
                String name = g.getName().toLowerCase();
                freq.put(name, freq.getOrDefault(name, 0) + 1);
            });
        }

        int total = freq.values().stream().mapToInt(i -> i).sum();
        if (total == 0) return Map.of();

        Map<String, Double> weights = new HashMap<>();
        for (var e : freq.entrySet()) {
            weights.put(e.getKey(), e.getValue() / (double) total);
        }

        return weights;
    }

    private double computeGenreScore(Set<String> movieGenres,
                                     Map<String, Double> userGenreWeights) {

        double sum = 0;
        for (String g : movieGenres) {
            Double w = userGenreWeights.get(g);
            if (w != null) sum += w;
        }
        return sum;
    }

    // =============================================================
    //   BAYESIAN AVERAGE
    // =============================================================
    private double computeBayesianScore(double avg, long count,
                                        double globalAvg, double m) {
        if (count <= 0) return globalAvg;
        return (count / (count + m)) * avg +
               (m / (count + m)) * globalAvg;
    }

    private Set<String> parseGenresCsv(String s) {
        if (s == null || s.isBlank()) return Set.of();
        return Arrays.stream(s.split(","))
                .map(String::trim).map(String::toLowerCase)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toSet());
    }

    // =============================================================
    //   DIVERSITY PICKER
    // =============================================================
    private List<Long> pickWithDiversity(Map<Integer, Double> baseScores,
                                         Map<Integer, String> primaryGenre,
                                         int limit) {

        List<Integer> remaining = new ArrayList<>(baseScores.keySet());
        Map<String, Integer> usage = new HashMap<>();
        List<Long> output = new ArrayList<>();

        while (!remaining.isEmpty() && output.size() < limit) {

            Integer best = null;
            double bestScore = -1_000_000;

            for (Integer id : remaining) {
                double base = baseScores.get(id);
                String g = primaryGenre.getOrDefault(id, "unknown");
                int used = usage.getOrDefault(g, 0);

                double effective = base / (1 + used);
                if (effective > bestScore) {
                    bestScore = effective;
                    best = id;
                }
            }

            if (best == null) break;

            output.add(best.longValue());
            String g = primaryGenre.getOrDefault(best, "unknown");
            usage.put(g, usage.getOrDefault(g, 0) + 1);
            remaining.remove(best);
        }

        return output;
    }

    // =============================================================
    //   FALLBACK POPULAR RECS
    // =============================================================
    private List<Long> fallbackGlobalRecommendations(Integer uid, int limit) {
        List<Object[]> rows = movieRepository.findAllWithDetailsNative();
        if (rows.isEmpty()) return List.of();

        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, String> primaryGenre = new HashMap<>();

        Double global = ratingRepository.findGlobalAverageRating();
        double globalAvg = (global != null) ? global : 3.5;
        double bayesM = 20.0;

        double maxBayes = 0, maxGenre = 0, maxPop = 0;

        Set<Integer> likedMovies = ratingRepository.findLikedByUser(uid).stream()
                .map(r -> r.getId().getMovieId())
                .collect(Collectors.toSet());
        Map<String, Double> userGenreWeights = buildUserGenreWeights(likedMovies);

        Map<Integer, Double> bayesMap = new HashMap<>();
        Map<Integer, Double> genreMap = new HashMap<>();
        Map<Integer, Double> popMap = new HashMap<>();

        for (Object[] row : rows) {
            Integer movieId = (Integer) row[0];
            Double avg = (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0;
            long count = (row[4] != null) ? ((Number) row[4]).longValue() : 0L;
            String csv = (String) row[5];

            Set<String> genres = parseGenresCsv(csv);
            primaryGenre.put(movieId, genres.stream().findFirst().orElse("unknown"));

            double bayes = computeBayesianScore(avg, count, globalAvg, bayesM);
            double pop = (count > 0) ? Math.log(1 + count) : 0;
            double genre = computeGenreScore(genres, userGenreWeights);

            bayesMap.put(movieId, bayes);
            popMap.put(movieId, pop);
            genreMap.put(movieId, genre);

            maxBayes = Math.max(maxBayes, bayes);
            maxGenre = Math.max(maxGenre, genre);
            maxPop = Math.max(maxPop, pop);
        }

        for (Integer id : bayesMap.keySet()) {
            double bayesNorm = (maxBayes > 0) ? bayesMap.get(id) / maxBayes : 0;
            double genreNorm = (maxGenre > 0) ? genreMap.get(id) / maxGenre : 0;
            double popNorm = (maxPop > 0) ? popMap.get(id) / maxPop : 0;

            double score =
                    0.5 * bayesNorm +
                    0.3 * genreNorm +
                    0.2 * popNorm;

            scores.put(id, score);
        }

        return pickWithDiversity(scores, primaryGenre, limit);
    }

    // =============================================================
    //   EXPLANATION GENERATOR
    // =============================================================
    private List<String> generateReasons(Movie movie, Long userId) {

        List<String> reasons = new ArrayList<>();

        Integer movieId = movie.getMovieId();

        // Genres
        Set<String> movieGenres = movie.getGenres().stream()
                .map(g -> g.getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<Integer> likedMovies = ratingRepository.findLikedByUser(userId.intValue())
                .stream()
                .map(r -> r.getId().getMovieId())
                .collect(Collectors.toSet());

        Map<String, Double> userGenreWeights = buildUserGenreWeights(likedMovies);

        for (String g : movieGenres) {
            if (userGenreWeights.containsKey(g)) {
                reasons.add("Matches your favorite genre: " + capitalize(g));
                break;
            }
        }

        // CF reason
        List<Integer> similarUsers =
                ratingRepository.findSimilarUsers(new ArrayList<>(likedMovies), userId.intValue());
        if (!similarUsers.isEmpty()) {
            reasons.add("Similar users also liked this");
        }

        // Popularity
        List<Object[]> stats = movieRepository.findDetailsForMovieIds(List.of(movieId));
        if (!stats.isEmpty()) {
            long count = ((Number) stats.get(0)[4]).longValue();
            if (count >= 50) {
                reasons.add("Highly rated and popular");
            }
        }

        // Release year
        Integer year = movie.getReleaseYear();
        if (year != null) {
            if (year < 2000) {
                reasons.add("A well-loved classic");
            } else if (year >= 2018) {
                reasons.add("Popular recent release");
            }
        }

        return reasons;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
