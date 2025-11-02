/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.time.LocalDateTime;

/**
 *
 * @author hn235
 */
public class GameDTO {
    private int scoreBlack;
    private int scoreWhite;
    private LocalDateTime endTime;
    private int playerWinnerId;
    private int playerBlackId;
    private int playerWhiteId;

    public GameDTO() {
    }

    public int getScoreBlack() {
        return scoreBlack;
    }

    public void setScoreBlack(int scoreBlack) {
        this.scoreBlack = scoreBlack;
    }

    public int getScoreWhite() {
        return scoreWhite;
    }

    public void setScoreWhite(int scoreWhite) {
        this.scoreWhite = scoreWhite;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getPlayerWinnerId() {
        return playerWinnerId;
    }

    public void setPlayerWinnerId(int playerWinnerId) {
        this.playerWinnerId = playerWinnerId;
    }

    public int getPlayerBlackId() {
        return playerBlackId;
    }

    public void setPlayerBlackId(int playerBlackId) {
        this.playerBlackId = playerBlackId;
    }

    public int getPlayerWhiteId() {
        return playerWhiteId;
    }

    public void setPlayerWhiteId(int playerWhiteId) {
        this.playerWhiteId = playerWhiteId;
    }

    
    
}
