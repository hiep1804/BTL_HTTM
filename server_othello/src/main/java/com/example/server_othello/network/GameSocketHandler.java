package com.example.server_othello.network;

import com.example.server_othello.model.Message;
import com.example.server_othello.model.Game;
import com.example.server_othello.model.Move;
import com.example.server_othello.model.User;
import com.example.server_othello.service.GameService;
import com.example.server_othello.service.MoveService;
import com.example.server_othello.service.UserService;
import com.example.server_othello.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
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
            User user = JsonUtils.convert(mess.getData(), User.class);
            userSessions.put(user.getId(), session);
            userIDWithSessionID.put(session.getId(), user.getId());
        }
        if(mess.getType().equals("add-move")) {
            Move move = JsonUtils.convert(mess.getData(), Move.class);
            int r=move.getRowIndex();
            int c=move.getCol();
            int turn=gameRegistry.getGames().get(move.getPlayerId()).getPlayerBlack().getId()==move.getPlayerId()?1:2;
            int board[][]=gameRegistry.getGames().get(move.getPlayerId()).getBoard();
            int currentPlayer=gameRegistry.getGames().get(move.getPlayerId()).getCurrentPlayerID();
            System.out.println(turn+" "+currentPlayer);
            if(checkMove(r,c,board,currentPlayer)&&turn==currentPlayer) {
                makeMove(r,c,session,move,board,currentPlayer);
                Game game=gameRegistry.getGames().get(move.getPlayerId());
                User user=userService.getUserById(move.getPlayerId()).orElse(null);
                //tạo move mới khi thỏa mãn tất cả điều kiện
                Move move1=new Move();
                move1.setGame(game);
                move1.setUser(user);
                move1.setRowIndex(move.getRowIndex());
                move1.setCol(move.getCol());
                moveService.createMove(move1);
                Message<Move> message1=new Message<>();
                message1.setType("update-turn");
                message1.setData(move);
                Message<Move> message2=new Message<>();
                message2.setType("update-turn");
                message2.setData(move);
                String json1=JsonUtils.toJson(message1);
                String json2=JsonUtils.toJson(message2);
                userSessions.get(game.getPlayerWhite().getId()).sendMessage(new TextMessage(json1));
                userSessions.get(game.getPlayerBlack().getId()).sendMessage(new TextMessage(json2));
                currentPlayer=(currentPlayer==1)?2:1;
                gameRegistry.getGames().get(game.getPlayerWhite().getId()).setCurrentPlayerID(currentPlayer);
                gameRegistry.getGames().get(game.getPlayerBlack().getId()).setCurrentPlayerID(currentPlayer);
            }
            else{
                Message<String> message2=new Message<>("move-error","Nước đi không hợp lệ");
                String json=JsonUtils.toJson(message2);
                session.sendMessage(new TextMessage(json));
            }
            for(int i=0;i<8;i++) {
                for(int j=0;j<8;j++) {
                    System.out.print(gameRegistry.getGames().get(move.getPlayerId()).getBoard()[i][j]+" ");
                }
                System.out.println();
            }
        }
        if(mess.getType().equals("user-exit")) {
            LocalDateTime end=LocalDateTime.now();
            User user1 = JsonUtils.convert(mess.getData(), User.class);
            Game game=gameRegistry.getGames().get(user1.getId());
            User userWinner=null;
            User userExit=null;
            if(game.getPlayerWhite().getId()==user1.getId()) {
                userWinner=game.getPlayerBlack();
                userExit=game.getPlayerWhite();
            }
            else{
                userWinner=game.getPlayerWhite();
                userExit=game.getPlayerBlack();
            }
            game.setPlayerWinner(userWinner);
            game.setEndTime(end);
            gameService.updateGame(game.getId(), game);
            userWinner.setEloRating(userWinner.getEloRating()+1);
            userService.updateUser(userWinner.getId(), userWinner);
            Message<User> message1=new Message<>("user-exit",userExit);
            String json=JsonUtils.toJson(message1);
            userSessions.get(userWinner.getId()).sendMessage(new TextMessage(json));
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
        return false;
    }
    private boolean checkMove(int row,int col,int board[][],int currentPlayer){
        if (board[row][col] != 0) {
            return false; // ô đã có quân
        }
        boolean valid = false;
        int opponent = (currentPlayer == 1) ? 2 : 1;

        // Duyệt 8 hướng
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            boolean hasOpponent = false;

            // Lướt qua quân đối thủ
            while (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == opponent) {
                hasOpponent = true;
                r += dir[0];
                c += dir[1];
            }

            // Nếu gặp quân mình sau quân đối thủ → hợp lệ
            if (hasOpponent && r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == currentPlayer) {
                valid = true;
            }
        }
        return valid;
    }
    private void makeMove(int row, int col,WebSocketSession session,Move move,int board[][],int currentPlayer) throws IOException {
        if (board[row][col] != 0) {
            return; // ô đã có quân
        }
        boolean valid = false;
        int opponent = (currentPlayer == 1) ? 2 : 1;

        // Duyệt 8 hướng
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            boolean hasOpponent = false;

            // Lướt qua quân đối thủ
            while (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == opponent) {
                hasOpponent = true;
                r += dir[0];
                c += dir[1];
            }

            // Nếu gặp quân mình sau quân đối thủ → hợp lệ
            if (hasOpponent && r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == currentPlayer) {
                valid = true;
                // Lật lại các quân đối thủ trên đường đi
                r = row + dir[0];
                c = col + dir[1];
                while (board[r][c] == opponent) {
                    board[r][c] = currentPlayer;
                    r += dir[0];
                    c += dir[1];
                }
            }
        }

        if (valid) {
            board[row][col] = currentPlayer;
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            if(!hasValidMove(currentPlayer,board)){
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                if(!hasValidMove(currentPlayer,board)){
                    showResult(session,move,board);
                }
                else{
                    Message<Move> message1=new Message<>();
                    message1.setType("change-turn");
                    String json=JsonUtils.toJson(message1);
                    Game game=gameRegistry.getGames().get(move.getPlayerId());
                    userSessions.get(game.getPlayerWhite().getId()).sendMessage(new TextMessage(json));
                    userSessions.get(game.getPlayerBlack().getId()).sendMessage(new TextMessage(json));
                }
            }
        }
    }
    private boolean hasValidMove(int player,int board[][]) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 0 && checkMove(i, j,board,player)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void showResult(WebSocketSession session,Move move,int board[][]) throws IOException {
        int black = 0, white = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 1) {
                    black++;
                }
                if (cell == 2) {
                    white++;
                }
            }
        }
        Game game=gameRegistry.getGames().get(move.getPlayerId());
        game.setEndTime(LocalDateTime.now());
        game.setScoreBlack(black);
        game.setScoreWhite(white);
        if(black>white){
            game.setPlayerWinner(game.getPlayerBlack());
            Message<String> message1 = new Message<>();
            Message<String> message2 = new Message<>();
            message1.setType("game-end");
            message2.setType("game-end");
            message1.setData("Bạn đã chiến thắng!");
            message2.setData("Bạn đã thua!");
            userSessions.get(game.getPlayerBlack().getId()).sendMessage(new TextMessage(JsonUtils.toJson(message1)));
            userSessions.get(game.getPlayerWhite().getId()).sendMessage(new TextMessage(JsonUtils.toJson(message2)));
        }
        if(white>black){
            game.setPlayerWinner(game.getPlayerWhite());
            Message<String> message1 = new Message<>();
            Message<String> message2 = new Message<>();
            message1.setType("game-end");
            message2.setType("game-end");
            message1.setData("Bạn đã chiến thắng!");
            message2.setData("Bạn đã thua!");
            userSessions.get(game.getPlayerBlack().getId()).sendMessage(new TextMessage(JsonUtils.toJson(message2)));
            userSessions.get(game.getPlayerWhite().getId()).sendMessage(new TextMessage(JsonUtils.toJson(message1)));
        }
        gameService.updateGame(game.getId(), game);
    }
}
