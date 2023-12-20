package com.example.myapi.user.model;

public interface UserRepository {
    int save(User user); // returns user id
    boolean existsByUsername(String username);
    User findUserByUsername(String username);
}
