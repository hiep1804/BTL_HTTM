package com.example.server_othello.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @OneToMany(mappedBy = "playerBlack", cascade = CascadeType.ALL)
    private List<Game> gamesAsBlack;

    @JsonIgnore
    @OneToMany(mappedBy = "playerWhite", cascade = CascadeType.ALL)
    private List<Game> gamesAsWhite;

    @JsonIgnore
    @OneToMany(mappedBy = "playerWinner", cascade = CascadeType.ALL)
    private List<Game> gamesWon;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Move> moves;
    @Transient
    private boolean status;
}