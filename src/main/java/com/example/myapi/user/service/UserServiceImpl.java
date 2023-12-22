package com.example.myapi.user.service;

import com.example.myapi.user.model.User;
import com.example.myapi.user.model.UserRepository;
import com.example.myapi.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public boolean checkUserExists(String username){
        return userRepository.existsByUsername(username);
    }

    public int createUser(User user){ // returns -1 if user already exists else user id
        if (!userRepository.existsByUsername(user.getUsername())) {
            return userRepository.save(user);
        } else {
            return -1;
        }
    }
    public boolean authenticateUser(User user) { // checks that user exists and that password matches
        if (userRepository.existsByUsername(user.getUsername())) {
            User verifyUser = userRepository.findUserByUsername(user.getUsername());
            return user.getPassword().equals(verifyUser.getPassword());
        }
        return false;
    }
}
