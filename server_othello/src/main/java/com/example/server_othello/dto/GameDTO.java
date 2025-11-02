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
public class GameDTO {
    private int scoreBlack;
    private int scoreWhite;
    private LocalDateTime endTime;
    private int playerWinnerId;
    private int playerBlackId;
    private int playerWhiteId;
}
