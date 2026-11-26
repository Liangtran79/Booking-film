package com.cinema.service;

import com.cinema.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Integer id);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    Optional<User> authenticate(String login, String password);
    User register(String username, String email, String rawPassword, String role, Boolean isStaff);
    User updateUser(Integer id, String username, String email, String role, Boolean isStaff);
    void updatePassword(Integer id, String newPassword);
    void deleteUser(Integer id);
}
