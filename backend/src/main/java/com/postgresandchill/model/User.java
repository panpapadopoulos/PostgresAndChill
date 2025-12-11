package com.postgresandchill.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Integer userId; // ✅ remove @GeneratedValue to avoid conflicts

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Getters / setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
