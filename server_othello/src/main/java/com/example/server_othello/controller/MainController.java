package com.example.server_othello.controller;

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
    public ResponseEntity<?> login(@RequestBody User loginBean) {
        User user=userService.findUserByLoginBeanDTO(loginBean);
        if(user == null) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu");
        }
        user.setStatus(true);
        return ResponseEntity.ok(user);
    }
}