package com.example.server_othello.network;

import com.example.server_othello.dto.GameDTO;
import com.example.server_othello.model.Game;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRegistry {
    private Map<Integer, Game> games=new ConcurrentHashMap<Integer, Game>();
    private Map<Integer, GameDTO> gameDTOMap=new ConcurrentHashMap<>();

    public Map<Integer, Game> getGames() {
        return games;
    }

    public void setGames(Map<Integer, Game> games) {
        this.games = games;
    }

    public Map<Integer, GameDTO> getGameDTOMap() {
        return gameDTOMap;
    }

    public void setGameDTOMap(Map<Integer, GameDTO> gameDTOMap) {
        this.gameDTOMap = gameDTOMap;
    }
}
