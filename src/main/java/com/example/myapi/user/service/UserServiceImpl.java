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

    public int createUser(User user){
        return userRepository.addUser(user);
    }
}
