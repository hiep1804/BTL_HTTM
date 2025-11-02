package com.example.server_othello.repository;

import com.example.server_othello.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Integer> {
    List<Move> findByGameId(int gameId);
}
