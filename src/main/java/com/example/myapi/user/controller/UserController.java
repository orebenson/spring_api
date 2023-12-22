package com.example.myapi.user.controller;

import com.example.myapi.jwt.service.TokenService;
import com.example.myapi.user.model.User;
import com.example.myapi.user.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Object> user() { // 'home' route, returns message to show valid connection
        Map<String, Object> body = new HashMap<>();
        body.put("message", "nice");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/user/new")
    public ResponseEntity<Object> newUser() { // new user route, returns user object for the sake of it
        User user = new User("username", "password");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody User user, BindingResult bindingResult) { //create new user. returns username from db if creation successful
        Map<String, Object> body = new HashMap<>();

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
        Map<String, Object> body = new HashMap<>();

        // Check that user is in the correct format
        if (bindingResult.hasErrors()) {
            body.put("error", "user must have username and password fields");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // authenticate that user password and username match
        boolean userIsAuthenticated = userService.authenticateUser(user);
        if (!userIsAuthenticated) {
            body.put("error", "invalid username or password");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // generate token from username
        Map<String, Object> tokenMap = tokenService.getToken(user.getUsername());
        String token = tokenMap.get("token").toString();
        String userFingerprint = tokenMap.get("fingerprint").toString();
        if (tokenMap.get("statusCode").equals(-1)) {
            System.out.println(tokenMap.get("status").toString());
            body.put("error", "user login failed");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        //Create headers and put JWT in header
        HttpHeaders headers = new HttpHeaders();
        HttpCookie cookie = new HttpCookie("token", token);
        headers.add("Set-Cookie", cookie.toString());

        // put secure cookie in header, using same site and httponly
        String fingerprintCookie = "__Secure-Fgp=" + userFingerprint + "; SameSite=Strict; HttpOnly; Secure";
        headers.add("Set-Cookie", fingerprintCookie);

        body.put("success", "you are logged in");
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /*
    Profile test to verify users token and return a message/their info if successful
    - take in http request
    - extract fingerprint cookie -> __Secure-Fgp=[fingerprint]
    - and jwt from authorization header -> Authorization="bearer: [jwt]"

    only allow if user is logged in (validate the JWT token received) -> return user info
     */
    @GetMapping("/profile")
    public ResponseEntity<Object> profile(@NotNull @RequestHeader(name = "Authorization") String jwt,
                                          @NotNull @CookieValue(name = "__Secure-Fgp") String fingerprint)
    {
        Map<String, Object> body = new HashMap<>();
        // remove the "Bearer" string part
        String token = jwt.split(" ")[1].trim();

        // validate token and get associated user
        Map<String, Object> tokenValidationMap = tokenService.validateToken(token, fingerprint);
        if (tokenValidationMap.get("statusCode").equals(-1)) {
            System.out.println(tokenValidationMap.get("status").toString());
            body.put("error", "token verification failed");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // if validation passes, return associated user and their id
        String username = tokenValidationMap.get("username").toString();

        //extra check for if user exists in database
        if (!userService.checkUserExists(username)) {
            body.put("error", "user verification failed");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        body.put("success", "verification successful, here is ur info");
        body.put("username", username);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

//    @PostMapping("/logout")
//    public ResponseEntity<Object> logout(JWT???) { // validate and revoke JWT
//
//    }

}
