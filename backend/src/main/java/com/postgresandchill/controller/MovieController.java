package com.postgresandchill.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.postgresandchill.repository.MovieRepository;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping
    public List<Object[]> getAllMovies() {
        return movieRepository.findAllWithDetailsNative();
    }
}
