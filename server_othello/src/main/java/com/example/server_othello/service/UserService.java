package com.example.server_othello.service;

import com.example.server_othello.dto.LoginBeanDTO;
import com.example.server_othello.model.User;
import com.example.server_othello.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(int id, User updatedUser) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setUsername(updatedUser.getUsername());
                    u.setPassword(updatedUser.getPassword());
                    u.setEloRating(updatedUser.getEloRating());
                    return userRepository.save(u);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }
    public User findUserByLoginBeanDTO(LoginBeanDTO loginBeanDTO) {
        return userRepository.findUserByUsernameAndPassword(loginBeanDTO.getUsername(), loginBeanDTO.getPassword());
    }
}