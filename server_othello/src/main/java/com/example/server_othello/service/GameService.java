package com.example.server_othello.service;

import com.example.server_othello.model.Game;
import com.example.server_othello.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(int id) {
        return gameRepository.findById(id);
    }

    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    public Game updateGame(int id, Game updatedGame) {
        return gameRepository.findById(id)
                .map(g -> {
                    g.setScoreBlack(updatedGame.getScoreBlack());
                    g.setScoreWhite(updatedGame.getScoreWhite());
                    g.setStartTime(updatedGame.getStartTime());
                    g.setEndTime(updatedGame.getEndTime());
                    g.setPlayerBlack(updatedGame.getPlayerBlack());
                    g.setPlayerWhite(updatedGame.getPlayerWhite());
                    g.setPlayerWinner(updatedGame.getPlayerWinner());
                    return gameRepository.save(g);
                })
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    public void deleteGame(int id) {
        gameRepository.deleteById(id);
    }
}