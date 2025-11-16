package com.example.server_othello.network;

import com.example.server_othello.model.Message;
import com.example.server_othello.model.User;
import com.example.server_othello.model.Game;
import com.example.server_othello.model.User;
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
    private Map<Integer, User> users = new ConcurrentHashMap<>();
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
            User user = JsonUtils.convert(mess.getData(), User.class);
            for (WebSocketSession session1 : userSessions.values()) {
                Message<User> message1 = new Message<>();
                message1.setType("add-user");
                message1.setData(user);
                String json = JsonUtils.toJson(message1);
                session1.sendMessage(new TextMessage(json));
            }
            List<User> userList = new ArrayList<>();
            for (User user1 : users.values()) {
                if(user1.getId()!=user.getId()){
                    userList.add(user1);
                }
            }
            Message<List<User>> message1 = new Message<>();
            message1.setType("load-all-user");
            message1.setData(userList);
            String json = JsonUtils.toJson(message1);
            session.sendMessage(new TextMessage(json));
            userSessions.put(user.getId(), session);
            users.put(user.getId(), user);
            userIDWithSessionID.put(session.getId(), user.getId());
        }
        if(mess.getType().equals("challenge")) {
            User user = JsonUtils.convert(mess.getData(), User.class);
            int id=userIDWithSessionID.get(session.getId());
            Message<User> message1 = new Message<>();
            message1.setType("challenge");
            message1.setData(users.get(id));
            String json = JsonUtils.toJson(message1);
            userSessions.get(user.getId()).sendMessage(new TextMessage(json));
        }
        if(mess.getType().equals("accept-challenge")) {
            User user = JsonUtils.convert(mess.getData(), User.class);
            if(users.get(user.getId())==null){
                Message<User> message1 = new Message<>("user-exited", user);
                String json = JsonUtils.toJson(message1);
                session.sendMessage(new TextMessage(json));
            }
            else {
                int id = userIDWithSessionID.get(session.getId());
                users.get(id).setStatus(false);
                users.get(user.getId()).setStatus(false);
                for (int id2 : users.keySet()) {
                    if (id2 != id && id2 != user.getId()) {
                        Message<String> message1 = new Message<>();
                        message1.setType("update-status");
                        message1.setData(id + " " + user.getId());
                        String json = JsonUtils.toJson(message1);
                        userSessions.get(id2).sendMessage(new TextMessage(json));
                    }
                }
                //tạo 1 game mới
                Game game = new Game();
                game.setStartTime(LocalDateTime.now());
                game.setPlayerBlack(userService.getUserById(user.getId()).orElse(null));
                game.setPlayerWhite(userService.getUserById(id).orElse(null));
                gameService.createGame(game);
                int board[][]=new int[8][8];
                board[3][3] = 2;
                board[3][4] = 1;
                board[4][3] = 1;
                board[4][4] = 2;
                game.setBoard(board);
                game.setCurrentPlayerID(1);
                gameRegistry.getGames().put(user.getId(), game);
                gameRegistry.getGames().put(id, game);
                Message<User> message1 = new Message<>();
                message1.setType("accept-challenge");
                message1.setData(users.get(id));
                String json = JsonUtils.toJson(message1);
                userSessions.get(user.getId()).sendMessage(new TextMessage(json));
                Message<User> message2 = new Message<>();
                message2.setType("to-accept-challenge");
                message2.setData(user);
                String json2 = JsonUtils.toJson(message2);
                session.sendMessage(new TextMessage(json2));
            }
        }
        if(mess.getType().equals("decline-challenge")){
            User user = JsonUtils.convert(mess.getData(), User.class);
            if(users.get(user.getId())!=null){
                int id=userIDWithSessionID.get(session.getId());
                Message<User> message1 = new Message<>("decline-challenge", users.get(id));
                String json = JsonUtils.toJson(message1);
                userSessions.get(user.getId()).sendMessage(new TextMessage(json));
            }
        }
        if(mess.getType().equals("user-exit")) {
            User user = JsonUtils.convert(mess.getData(), User.class);
            int id=user.getId();
            userSessions.remove(id);
            users.remove(id);
            Message<User> message1 = new Message<>();
            message1.setType("user-exit");
            message1.setData(user);
            String json = JsonUtils.toJson(message1);
            for(int id2 : userSessions.keySet()) {
                if(id2!=user.getId()) {
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
