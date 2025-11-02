package com.example.server_othello.repository;

import com.example.server_othello.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    User findUserByUsernameAndPassword(String username, String password);
}