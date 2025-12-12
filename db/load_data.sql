\echo ' Importing data from /data ...'

-- ======================================
-- 1 Import movies & genres
-- ======================================
-- Load movies
CREATE TEMP TABLE tmp_movies (
    movie_id INT,
    title TEXT,
    genres TEXT
);

COPY tmp_movies(movie_id, title, genres)
    FROM '/data/movies.csv'
    DELIMITER ',' CSV HEADER;

-- Insert movies (extract year if present, else NULL)
INSERT INTO movies (movie_id, title, release_year)
SELECT
    movie_id,
    regexp_replace(title, '\s*\(\d{4}\)$', ''),  -- Clean "(1995)" if present
    CASE
        WHEN title ~ '\(\d{4}\)$'
        THEN CAST(regexp_replace(title, '.*\((\d{4})\)$', '\1') AS INT)
        ELSE NULL
    END AS release_year
FROM tmp_movies;

-- Insert genres
INSERT INTO genres (name)
SELECT DISTINCT unnest(string_to_array(genres, '|')) AS name
FROM tmp_movies
ON CONFLICT (name) DO NOTHING;

-- Insert movie_genre relationships only for existing movies
INSERT INTO movie_genres (movie_id, genre_id)
SELECT
    t.movie_id,
    g.genre_id
FROM tmp_movies t
JOIN LATERAL unnest(string_to_array(t.genres, '|')) AS genre_name(name) ON TRUE
JOIN genres g ON g.name = genre_name.name
JOIN movies m ON m.movie_id = t.movie_id;  -- ensures movie exists

DROP TABLE tmp_movies;


-- ======================================
-- 2️ Create users dynamically from ratings.csv
-- ======================================
CREATE TEMP TABLE tmp_ratings (
    user_id INT,
    movie_id INT,
    rating FLOAT,
    timestamp BIGINT
);

COPY tmp_ratings(user_id, movie_id, rating, timestamp)
FROM '/data/ratings.csv'
DELIMITER ',' CSV HEADER;

-- Create dummy users for all unique user_ids from ratings
INSERT INTO users (user_id, username, password)
SELECT DISTINCT user_id, 'user_' || user_id, 'password'
FROM tmp_ratings
ON CONFLICT (user_id) DO NOTHING;

-- Insert ratings (now FK is valid)
INSERT INTO ratings (user_id, movie_id, rating, timestamp)
SELECT user_id, movie_id, rating, timestamp
FROM tmp_ratings;

DROP TABLE tmp_ratings;

-- ======================================
-- 3 Load tags
-- ======================================
CREATE TEMP TABLE tmp_tags (
    user_id INT,
    movie_id INT,
    tag TEXT,
    timestamp BIGINT
);

COPY tmp_tags(user_id, movie_id, tag, timestamp)
FROM '/data/tags.csv'
DELIMITER ',' CSV HEADER;

-- Create any missing users (tags has a few extra sometimes)
INSERT INTO users (user_id, username, password)
SELECT DISTINCT user_id, 'user_' || user_id, 'password'
FROM tmp_tags
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO tags (user_id, movie_id, tag, timestamp)
SELECT user_id, movie_id, tag, timestamp
FROM tmp_tags;

DROP TABLE tmp_tags;

\echo 'Data import complete!'
