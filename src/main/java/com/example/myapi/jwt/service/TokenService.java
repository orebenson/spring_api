package com.example.myapi.jwt.service;

public interface TokenService {
    String getToken(String username); // generate token using username as subject and return
    String validateToken(String token); // validate, decode, return username
    void revokeToken(String token); // revoke token
}
