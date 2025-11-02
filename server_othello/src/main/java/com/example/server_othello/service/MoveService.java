package com.example.server_othello.service;

import com.example.server_othello.model.Move;
import com.example.server_othello.repository.MoveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MoveService {

    private final MoveRepository moveRepository;

    public List<Move> getAllMoves() {
        return moveRepository.findAll();
    }

    public Optional<Move> getMoveById(int id) {
        return moveRepository.findById(id);
    }

    public List<Move> getMovesByGameId(int gameId) {
        return moveRepository.findByGameId(gameId);
    }

    public Move createMove(Move move) {
        return moveRepository.save(move);
    }

    public Move updateMove(int id, Move updatedMove) {
        return moveRepository.findById(id)
                .map(m -> {
                    m.setRowIndex(updatedMove.getRowIndex());
                    m.setCol(updatedMove.getCol());
                    m.setUser(updatedMove.getUser());
                    m.setGame(updatedMove.getGame());
                    return moveRepository.save(m);
                })
                .orElseThrow(() -> new RuntimeException("Move not found"));
    }

    public void deleteMove(int id) {
        moveRepository.deleteById(id);
    }
}
