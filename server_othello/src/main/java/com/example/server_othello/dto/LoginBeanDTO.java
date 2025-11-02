package com.example.server_othello.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginBeanDTO {
    private String username;
    private String password;
}
