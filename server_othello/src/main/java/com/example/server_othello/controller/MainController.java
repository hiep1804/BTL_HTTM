package com.example.server_othello.controller;

import com.example.server_othello.dto.LoginBeanDTO;
import com.example.server_othello.dto.UserDTO;
import com.example.server_othello.model.User;
import com.example.server_othello.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class MainController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginBeanDTO loginBeanDTO) {
        User user=userService.findUserByLoginBeanDTO(loginBeanDTO);
        if(user == null) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu");
        }
        UserDTO userDTO=new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setEmail(user.getEmail());
        userDTO.setEloRating(user.getEloRating()==null?0:user.getEloRating());
        return ResponseEntity.ok(userDTO);
    }
}