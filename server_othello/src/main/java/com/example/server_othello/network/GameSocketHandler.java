package com.example.server_othello.network;

import com.example.server_othello.dto.GameDTO;
import com.example.server_othello.dto.Message;
import com.example.server_othello.dto.MoveDTO;
import com.example.server_othello.dto.UserDTO;
import com.example.server_othello.model.Game;
import com.example.server_othello.model.Move;
import com.example.server_othello.model.User;
import com.example.server_othello.service.GameService;
import com.example.server_othello.service.MoveService;
import com.example.server_othello.service.UserService;
import com.example.server_othello.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends TextWebSocketHandler {
    @Autowired
    private GameService gameService;
    @Autowired
    private UserService userService;
    @Autowired
    private MoveService moveService;
    @Autowired
    private GameRegistry gameRegistry;
    private Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private Map<String, Integer> userIDWithSessionID = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New client connected: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Message from " + session.getId() + ": " + message.getPayload());
        Message<?> mess = JsonUtils.fromJson(message.getPayload(), Message.class);
        if(mess.getType().equals("new-player")) {
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            userSessions.put(userDTO.getId(), session);
            userIDWithSessionID.put(session.getId(), userDTO.getId());
        }
        if(mess.getType().equals("add-move")) {
            MoveDTO moveDTO = JsonUtils.convert(mess.getData(), MoveDTO.class);
            Game game=gameRegistry.getGames().get(moveDTO.getPlayerId());
            User user=userService.getUserById(moveDTO.getPlayerId()).orElse(null);
            Move move=new Move();
            move.setGame(game);
            move.setUser(user);
            move.setRowIndex(moveDTO.getRow());
            move.setCol(moveDTO.getCol());
            moveService.createMove(move);
            Message<MoveDTO> message1=new Message<>();
            message1.setType("update-turn");
            if(game.getPlayerBlack().getId()==user.getId()) {
                message1.setData(moveDTO);
                String json=JsonUtils.toJson(message1);
                userSessions.get(game.getPlayerWhite().getId()).sendMessage(new TextMessage(json));
            }
            else{
                message1.setData(moveDTO);
                String json=JsonUtils.toJson(message1);
                userSessions.get(game.getPlayerBlack().getId()).sendMessage(new TextMessage(json));
            }
        }
        if(mess.getType().equals("sent-result-game")) {
            GameDTO gameDTO = JsonUtils.convert(mess.getData(), GameDTO.class);
            gameDTO.setEndTime(LocalDateTime.now());
            if(gameDTO.getPlayerBlackId()==0){
                User user=userService.getUserById(gameDTO.getPlayerWhiteId()).orElse(null);
                Game game=gameRegistry.getGames().get(user.getId());
                game.setPlayerWhite(user);
                game.setScoreBlack(gameDTO.getScoreBlack());
                game.setScoreWhite(gameDTO.getScoreWhite());
                game.setEndTime(gameDTO.getEndTime());
                if(gameDTO.getPlayerWinnerId()!=0){
                    user.setEloRating(user.getEloRating()+1);
                    userService.updateUser(user.getId(), user);
                    game.setPlayerWinner(user);
                }
                gameService.updateGame(game.getId(), game);
            }
            if(gameDTO.getPlayerWhiteId()==0){
                User user=userService.getUserById(gameDTO.getPlayerBlackId()).orElse(null);
                Game game=gameRegistry.getGames().get(user.getId());
                game.setPlayerBlack(user);
                game.setScoreBlack(gameDTO.getScoreBlack());
                game.setScoreWhite(gameDTO.getScoreWhite());
                game.setEndTime(gameDTO.getEndTime());
                if(gameDTO.getPlayerWinnerId()!=0){
                    user.setEloRating(user.getEloRating()+1);
                    userService.updateUser(user.getId(), user);
                    game.setPlayerWinner(user);
                }
                gameService.updateGame(game.getId(), game);
            }
        }
        if(mess.getType().equals("user-exit")) {
            LocalDateTime end=LocalDateTime.now();
            GameDTO gameDTO = JsonUtils.convert(mess.getData(), GameDTO.class);
            int id=0;
            Game game=null;
            User user=null;
            User user_exit=null;
            if(gameDTO.getPlayerBlackId()!=0){
                id=gameDTO.getPlayerBlackId();
                game=gameRegistry.getGames().get(id);
                user=game.getPlayerWhite();
                game.setPlayerWinner(game.getPlayerWhite());
                user_exit=game.getPlayerBlack();
            }
            else{
                id=gameDTO.getPlayerWhiteId();
                game=gameRegistry.getGames().get(id);
                game.setPlayerWinner(game.getPlayerBlack());
                user=game.getPlayerBlack();
                user_exit=game.getPlayerWhite();
            }
            game.setEndTime(end);
            gameService.updateGame(game.getId(), game);
            user.setEloRating(user.getEloRating()+1);
            userService.updateUser(user.getId(), user);
            UserDTO userDTO=new UserDTO();
            userDTO.setId(user_exit.getId());
            userDTO.setUsername(user_exit.getUsername());
            userDTO.setPassword(user_exit.getPassword());
            userDTO.setEmail(user_exit.getEmail());
            Message<UserDTO> message1=new Message<>("user-exit",userDTO);
            String json=JsonUtils.toJson(message1);
            userSessions.get(user.getId()).sendMessage(new TextMessage(json));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error from " + session.getId() + ": " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Client disconnected: " + session.getId());
        gameRegistry.getGames().remove(userIDWithSessionID.get(session.getId()));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false; // Không chia nhỏ message
    }
}
