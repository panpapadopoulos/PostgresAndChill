package com.postgresandchill.repository;

import com.postgresandchill.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    // Get the highest user_id currently in the database
    @Query("SELECT COALESCE(MAX(u.userId), 0) FROM User u")
    int getMaxUserId();
}
