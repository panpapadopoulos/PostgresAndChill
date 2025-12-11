package com.postgresandchill.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.postgresandchill.model.Movie;

import java.util.List;

public interface MovieRepository extends CrudRepository<Movie, Integer> {

    /* ========================================================================================
       SMART POPULAR MOVIES — Bayesian Weighted Ranking
       ======================================================================================== */
    @Query(value = """
        SELECT 
            m.movie_id AS movieId,
            m.title AS title,
            m.release_year AS releaseYear,

            COALESCE(ROUND(AVG(DISTINCT r.rating)::numeric, 2), 0) AS avgRating,
            COUNT(DISTINCT r.user_id) AS ratingCount,
            COALESCE(string_agg(DISTINCT g.name, ','), '') AS genresCsv,

            (
                (COUNT(r.user_id)::float / (COUNT(r.user_id) + 20.0)) * COALESCE(AVG(r.rating), 3.5)
                +
                (20.0 / (COUNT(r.user_id) + 20.0)) * 3.5
            ) AS weightedRating

        FROM movies m
        LEFT JOIN ratings r       ON r.movie_id = m.movie_id
        LEFT JOIN movie_genres mg ON mg.movie_id = m.movie_id
        LEFT JOIN genres g        ON g.genre_id = mg.genre_id

        GROUP BY m.movie_id, m.title, m.release_year
        ORDER BY weightedRating DESC
        """,
        nativeQuery = true)
    List<Object[]> findAllWithDetailsNative();


    /* ========================================================================================
       DETAILS FOR SPECIFIC MOVIE IDS — used by CF recommendations
       ======================================================================================== */
    @Query(value = """
        SELECT 
            m.movie_id AS movieId,
            m.title AS title,
            m.release_year AS releaseYear,

            COALESCE(ROUND(AVG(DISTINCT r.rating)::numeric, 3), 0) AS avgRating,
            COUNT(DISTINCT r.user_id) AS ratingCount,
            COALESCE(string_agg(DISTINCT g.name, ','), '') AS genresCsv

        FROM movies m
        LEFT JOIN ratings r       ON r.movie_id = m.movie_id
        LEFT JOIN movie_genres mg ON mg.movie_id = m.movie_id
        LEFT JOIN genres g        ON g.genre_id = mg.genre_id

        WHERE m.movie_id IN (:ids)

        GROUP BY m.movie_id, m.title, m.release_year
        """,
        nativeQuery = true)
    List<Object[]> findDetailsForMovieIds(@Param("ids") List<Integer> ids);

}
