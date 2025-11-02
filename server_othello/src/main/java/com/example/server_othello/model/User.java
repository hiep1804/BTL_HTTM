package com.example.server_othello.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255)
    private String email;
    @Column(nullable = false)
    private Integer eloRating;

    // Mối quan hệ với Games (playerBlack, playerWhite, playerWinner)
    @OneToMany(mappedBy = "playerBlack", cascade = CascadeType.ALL)
    private List<Game> gamesAsBlack;

    @OneToMany(mappedBy = "playerWhite", cascade = CascadeType.ALL)
    private List<Game> gamesAsWhite;

    @OneToMany(mappedBy = "playerWinner", cascade = CascadeType.ALL)
    private List<Game> gamesWon;

    // Mối quan hệ với Moves
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Move> moves;
}