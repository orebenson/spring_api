package com.example.myapi.jwt.service;

import java.util.Map;

public interface TokenService {
    Map<String, Object> getToken(String username); // generate token using username as subject and return
    Map<String, Object> validateToken(String token, String fingerprint); // validate, decode, return username
    void revokeToken(String token); // revoke token
}
