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

    public int save(User user) { //check that the user doesnt already exist in the database and return -1 if they do
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        String selectSql = "SELECT user_id FROM users WHERE username = ?";

        jdbc.update(insertSql, user.getUsername(), user.getPassword());

        int id = jdbc.queryForObject(selectSql, Integer.class, user.getUsername());

        return id;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE username = ?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, username));
    }
    public User findUserByUsername(String username) {
        String sql = "SELECT username, password FROM users WHERE username = ?";
        return jdbc.queryForObject(sql, userMapper, username);
    }
}
