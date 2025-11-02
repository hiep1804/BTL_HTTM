package com.example.server_othello.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private int id;
    private String username;
    private String password;
    private String email;
    private int eloRating;
    public String toString(){
        return id + " " + username + " " + password + " " + email + " " + eloRating;
    }
}
