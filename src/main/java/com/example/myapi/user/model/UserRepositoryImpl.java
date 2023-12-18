package com.example.myapi.user.model;

import com.example.myapi.user.model.User;
import com.example.myapi.user.model.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    JdbcTemplate jdbc;
    private RowMapper<User> userMapper;
    public UserRepositoryImpl(JdbcTemplate aJdbc){
        this.jdbc = aJdbc;
        setUserMapper();
    }
    private void setUserMapper(){
        this.userMapper = (resultSet, i) -> {
            User user = new User();
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            return user;
        };
    }

    public int addUser(User user) {
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        String selectSql = "SELECT user_id FROM users WHERE username = ?";

        jdbc.update(insertSql, user.getUsername(), user.getPassword());

        int id = jdbc.queryForObject(selectSql, Integer.class, user.getUsername());

        return id;
    }
}
