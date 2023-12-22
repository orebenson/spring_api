package com.example.myapi.user.controller;

import com.example.myapi.jwt.service.TokenService;
import com.example.myapi.user.model.User;
import com.example.myapi.user.service.UserService;
import org.apache.el.parser.Token;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    UserService userService;
    TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping(value = {"/"})
    public ResponseEntity<Object> user() throws URISyntaxException { // 'home' route, returns message to show valid connection
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "nice");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/user/new")
    public ResponseEntity<Object> newUser() throws URISyntaxException { // new user route, returns user object for the sake of it
        User user = new User("username", "password");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody User user, BindingResult bindingResult) { //create new user. returns username from db if creation successful
        Map<String, Object> body = new HashMap<String, Object>();

        // Check that user is in the correct format
        if (bindingResult.hasErrors()) {
            body.put("error", "user must have username and password fields");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        int id = userService.createUser(user);
        if (id < 0) { // userService will return -1 if the user already exists
            body.put("error", "user already exists");
            return new ResponseEntity<>(body, HttpStatus.I_AM_A_TEAPOT);
        }
        body.put("your userid", id);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User user, BindingResult bindingResult) { // authenticate a user -> generate jwt token -> return token in a header
        Map<String, Object> body = new HashMap<String, Object>();

        // Check that user is in the correct format
        if (bindingResult.hasErrors()) {
            body.put("error", "user must have username and password fields");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // authenticate that user password and username match
        boolean userIsAuthenticated = userService.authenticateUser(user);
        if(!userIsAuthenticated) {
            body.put("error", "invalid username or password");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // generate token from username
        Map<String, String> tokenMap = tokenService.getToken(user.getUsername());
        String token = tokenMap.get("token");
        String userFingerprint = tokenMap.get("fingerprint");
        System.out.println(tokenMap.get("status"));

        //Create headers and put JWT in header
        HttpHeaders headers = new HttpHeaders();
        HttpCookie cookie = new HttpCookie("token", token);
        headers.add("Set-Cookie", cookie.toString());
        // put secure cookie in header, using samesite and httponly
        String fingerprintCookie = "__Secure-Fgp=" + userFingerprint + "; SameSite=Strict; HttpOnly; Secure";
        headers.add("Set-Cookie", fingerprintCookie);

        body.put("success", "you are logged in");
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

//    @GetMapping("/profileinfo")
//    public ResponseEntity<Object> profile(JWT???) { // only allow if user is logged in (validate the JWT token received) -> return user info
//
//    }

//    @PostMapping("/logout")
//    public ResponseEntity<Object> logout(JWT???) { // validate and revoke JWT
//
//    }

}
