package com.example.myapi.jwt.service;

import java.util.Map;

public interface TokenService {
    Map<String, String> getToken(String username); // generate token using username as subject and return
    String validateToken(String token); // validate, decode, return username
    void revokeToken(String token); // revoke token
}
