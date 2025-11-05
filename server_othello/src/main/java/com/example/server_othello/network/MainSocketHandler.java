package com.example.server_othello.network;

import com.example.server_othello.dto.GameDTO;
import com.example.server_othello.dto.Message;
import com.example.server_othello.dto.UserDTO;
import com.example.server_othello.model.Game;
import com.example.server_othello.service.GameService;
import com.example.server_othello.service.UserService;
import com.example.server_othello.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MainSocketHandler extends TextWebSocketHandler {
    private Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private Map<Integer, UserDTO> userDTOs = new ConcurrentHashMap<>();
    private Map<String, Integer> userIDWithSessionID = new ConcurrentHashMap<>();
    @Autowired
    private GameRegistry gameRegistry;
    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New client connected: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Message from " + session.getId() + ": " + message.getPayload());
        Message<?> mess = JsonUtils.fromJson(message.getPayload(), Message.class);
        if(mess.getType().equals("new-user")) {
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            for (WebSocketSession session1 : userSessions.values()) {
                Message<UserDTO> message1 = new Message<>();
                message1.setType("add-user");
                message1.setData(userDTO);
                String json = JsonUtils.toJson(message1);
                session1.sendMessage(new TextMessage(json));
            }
            List<UserDTO> userDTOList = new ArrayList<>();
            for (UserDTO userDTO1 : userDTOs.values()) {
                if(userDTO1.getId()!=userDTO.getId()){
                    userDTOList.add(userDTO1);
                }
            }
            Message<List<UserDTO>> message1 = new Message<>();
            message1.setType("load-all-user");
            message1.setData(userDTOList);
            String json = JsonUtils.toJson(message1);
            session.sendMessage(new TextMessage(json));
            userSessions.put(userDTO.getId(), session);
            userDTOs.put(userDTO.getId(), userDTO);
            userIDWithSessionID.put(session.getId(), userDTO.getId());
        }
        if(mess.getType().equals("challenge")) {
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            int id=userIDWithSessionID.get(session.getId());
            Message<UserDTO> message1 = new Message<>();
            message1.setType("challenge");
            message1.setData(userDTOs.get(id));
            String json = JsonUtils.toJson(message1);
            userSessions.get(userDTO.getId()).sendMessage(new TextMessage(json));
        }
        if(mess.getType().equals("accept-challenge")) {
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            if(userDTOs.get(userDTO.getId())==null){
                Message<UserDTO> message1 = new Message<>("user-exited", userDTO);
                String json = JsonUtils.toJson(message1);
                session.sendMessage(new TextMessage(json));
            }
            else {
                int id = userIDWithSessionID.get(session.getId());
                userDTOs.get(id).setStatus(false);
                userDTOs.get(userDTO.getId()).setStatus(false);
                for (int id2 : userDTOs.keySet()) {
                    if (id2 != id && id2 != userDTO.getId()) {
                        Message<String> message1 = new Message<>();
                        message1.setType("update-status");
                        message1.setData(id + " " + userDTO.getId());
                        String json = JsonUtils.toJson(message1);
                        userSessions.get(id2).sendMessage(new TextMessage(json));
                    }
                }
                //tạo 1 game mới
                Game game = new Game();
                game.setStartTime(LocalDateTime.now());
                game.setPlayerBlack(userService.getUserById(userDTO.getId()).orElse(null));
                game.setPlayerWhite(userService.getUserById(id).orElse(null));
                gameService.createGame(game);
                gameRegistry.getGames().put(userDTO.getId(), game);
                gameRegistry.getGames().put(id, game);
                GameDTO gameDTO = new GameDTO();
                gameDTO.setPlayerWhiteID(id);
                gameDTO.setPlayerBlackID(userDTO.getId());
                int board[][]=new int[8][8];
                board[3][3] = 2;
                board[3][4] = 1;
                board[4][3] = 1;
                board[4][4] = 2;
                gameDTO.setBoard(board);
                gameDTO.setCurrentPlayerID(1);
                gameRegistry.getGameDTOMap().put(id, gameDTO);
                gameRegistry.getGameDTOMap().put(userDTO.getId(), gameDTO);
                Message<UserDTO> message1 = new Message<>();
                message1.setType("accept-challenge");
                message1.setData(userDTOs.get(id));
                String json = JsonUtils.toJson(message1);
                userSessions.get(userDTO.getId()).sendMessage(new TextMessage(json));
                Message<UserDTO> message2 = new Message<>();
                message2.setType("to-accept-challenge");
                message2.setData(userDTO);
                String json2 = JsonUtils.toJson(message2);
                session.sendMessage(new TextMessage(json2));
            }
        }
        if(mess.getType().equals("decline-challenge")){
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            if(userDTOs.get(userDTO.getId())!=null){
                int id=userIDWithSessionID.get(session.getId());
                Message<UserDTO> message1 = new Message<>("decline-challenge", userDTOs.get(id));
                String json = JsonUtils.toJson(message1);
                userSessions.get(userDTO.getId()).sendMessage(new TextMessage(json));
            }
        }
        if(mess.getType().equals("user-exit")) {
            UserDTO userDTO = JsonUtils.convert(mess.getData(), UserDTO.class);
            int id=userDTO.getId();
            userSessions.remove(id);
            userDTOs.remove(id);
            Message<UserDTO> message1 = new Message<>();
            message1.setType("user-exit");
            message1.setData(userDTO);
            String json = JsonUtils.toJson(message1);
            for(int id2 : userSessions.keySet()) {
                if(id2!=userDTO.getId()) {
                    userSessions.get(id2).sendMessage(new TextMessage(json));
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error from " + session.getId() + ": " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Client disconnected: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false; // Không chia nhỏ message
    }
}
