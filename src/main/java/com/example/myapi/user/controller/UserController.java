package com.example.myapi.user.controller;

import com.example.myapi.user.model.User;
import com.example.myapi.user.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = {"/"})
    public ResponseEntity<Object> user() throws URISyntaxException { // 'home' route, returns message to show valid connection
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "nice");

        HttpHeaders headers = new HttpHeaders();
        HttpCookie cookie = new HttpCookie("mycookie", "cookieval");

        headers.add("Set-Cookie", cookie.toString());
        headers.add("myheader", "myheadervalue");

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/user/new")
    public ResponseEntity<Object> newUser() throws URISyntaxException { // new user route, returns user object for the sake of it
        User user = new User("username", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.add("myheader", "myheadervalue");
        headers.setLocation(new URI("/")); // re-routes to home

        return new ResponseEntity<>(user, headers, HttpStatus.OK);
    }

    @PostMapping("/user/new")
    public ResponseEntity<Object> createUser(@RequestBody User user, BindingResult bindingResult) { //create new user. returns username from db if creation successful
        Map<String, Object> body = new HashMap<String, Object>();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        int id = userService.createUser(user);
        body.put("your userid", id);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

}
