-- Project: postgres-and-chill
-- Author: Panagiotis Papadopoulos (PNW)
-- School: PNW
-- PostgreSQL schema

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE movies (
    movie_id INT PRIMARY KEY,
    title TEXT NOT NULL,
    release_year INT
);


CREATE TABLE genres (
    genre_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE movie_genres (
    movie_id INT REFERENCES movies(movie_id),
    genre_id INT REFERENCES genres(genre_id),
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE ratings (
    user_id INT REFERENCES users(user_id),
    movie_id INT REFERENCES movies(movie_id),
    rating FLOAT CHECK (rating >= 0 AND rating <= 5),
    timestamp BIGINT,
    PRIMARY KEY (user_id, movie_id)
);

CREATE TABLE tags (
    user_id INT REFERENCES users(user_id),
    movie_id INT REFERENCES movies(movie_id),
    tag TEXT,
    timestamp BIGINT
);
