package com.example.myapi.user.service;

import com.example.myapi.user.model.User;

public interface UserService {
    int createUser(User user);
    boolean authenticateUser(User user);
    boolean checkUserExists(User user);
}
