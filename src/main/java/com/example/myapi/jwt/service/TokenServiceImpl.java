package com.example.myapi.jwt.service;

import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService{
    @Override
    public String getToken(String username) {
        return null;
    }

    @Override
    public String validateToken(String token) {
        return null;
    }

    @Override
    public void revokeToken(String token) {

    }
}
