package com.postgresandchill.service;

import com.postgresandchill.dto.MovieDTO;
import com.postgresandchill.repository.MovieRepository;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDTO> getAllMovies() {
        List<Object[]> results = movieRepository.findAllWithDetailsNative();

        return results.stream()
                .map(row -> new MovieDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        row[2] != null ? ((Number) row[2]).intValue() : null,
                        row[3] != null ? ((Number) row[3]).floatValue() : 0f,
                        row[4] != null
                                ? Arrays.asList((String[]) row[4])
                                : List.of()
                ))
                .collect(Collectors.toList());
    }
}
