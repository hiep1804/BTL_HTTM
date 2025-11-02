package com.example.server_othello.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private int scoreBlack;
    @Column(nullable = false)
    private int scoreWhite;
    @Column(nullable = false)
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Quan hệ với Users
    @ManyToOne
    @JoinColumn(name = "player_black_id")
    private User playerBlack;

    @ManyToOne
    @JoinColumn(name = "player_white_id")
    private User playerWhite;

    @ManyToOne
    @JoinColumn(name = "player_winner_id")
    private User playerWinner;

    // Quan hệ với Moves
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Move> moves;
}
