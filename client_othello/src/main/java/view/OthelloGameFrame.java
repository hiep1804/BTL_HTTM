/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.MainPlayerController;
import controller.OthelloGameController;
import controller.UIListener;
import dto.GameDTO;
import dto.Message;
import dto.MoveDTO;
import dto.UserDTO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.WebSocketClientManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import util.JsonUtils;

public class OthelloGameFrame extends JFrame implements UIListener {

    private static final int SIZE = 8;
    private JButton[][] board = new JButton[SIZE][SIZE];
    private int[][] state = new int[SIZE][SIZE]; // 0=trống, 1=đen, 2=trắng
    private int currentPlayer = 1; // 1=đen, 2=trắng
    private JLabel statusLabel;
    private UserDTO user;
    private int turn;
    private OthelloGameController othelloGameController;
    private MainPlayerController mainPlayerController;

    public OthelloGameFrame(UserDTO user, int turn) {
        this.user = user;
        this.turn = turn;
        othelloGameController = new OthelloGameController(this);
        othelloGameController.connect();
        mainPlayerController = new MainPlayerController();
        mainPlayerController.setStatus(false);
        mainPlayerController.connect();
        initUI();
    }

    private void initUI() {
        setTitle("Othello 2 Người Chơi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Font font = new Font("Arial", Font.BOLD, 54);

        // Khởi tạo bàn cờ
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton btn = new JButton();
                btn.setBackground(new Color(0, 153, 0));
                btn.setFont(font);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                int row = i, col = j;
                btn.addActionListener(e -> handleClick(row, col));
                board[i][j] = btn;
                boardPanel.add(btn);
            }
        }

        // 4 quân khởi đầu
        state[3][3] = 2;
        state[3][4] = 1;
        state[4][3] = 1;
        state[4][4] = 2;
        updateBoard();
        //chuỗi hiển thị lượt của ai
        String s = "";
        if (turn == 1) {
            s = "Lượt của bạn (●)";
        } else {
            s = "Lượt của đối thủ (○)";
        }
        statusLabel = new JLabel(s, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitGame();
                Message<UserDTO> message = new Message<>("user-exit", user);
                mainPlayerController.send(message);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    //hàm xử lí khi chọn 1 nước đi
    private void handleClick(int row, int col) {
        if (turn == currentPlayer) {
            updateTurn(row, col);
            makeMove(row, col);
        }
    }

    private void makeMove(int row, int col) {
        if (state[row][col] != 0) {
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
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && state[r][c] == opponent) {
                hasOpponent = true;
                r += dir[0];
                c += dir[1];
            }

            // Nếu gặp quân mình sau quân đối thủ → hợp lệ
            if (hasOpponent && r >= 0 && r < SIZE && c >= 0 && c < SIZE && state[r][c] == currentPlayer) {
                valid = true;
                // Lật lại các quân đối thủ trên đường đi
                r = row + dir[0];
                c = col + dir[1];
                while (state[r][c] == opponent) {
                    state[r][c] = currentPlayer;
                    r += dir[0];
                    c += dir[1];
                }
            }
        }

        if (valid) {
            state[row][col] = currentPlayer;
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            updateBoard();

            if (!hasValidMove(currentPlayer)) {
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                if (!hasValidMove(currentPlayer)) {
                    showResult();
                } else {
                    JOptionPane.showMessageDialog(this, "Không có nước đi hợp lệ! Đổi lượt!");
                }
            }

            updateStatus();
        }
    }

    private void updateTurn(int r, int c) {
        if (checkMove(r, c)) {
            MoveDTO moveDTO = new MoveDTO();
            moveDTO.setCol(c);
            moveDTO.setRow(r);
            moveDTO.setPlayerId(user.getId());
            Message<MoveDTO> message = new Message<>("add-move", moveDTO);
            othelloGameController.send(message);
        }
    }

    private boolean checkMove(int row, int col) {
        if (state[row][col] != 0) {
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
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && state[r][c] == opponent) {
                hasOpponent = true;
                r += dir[0];
                c += dir[1];
            }

            // Nếu gặp quân mình sau quân đối thủ → hợp lệ
            if (hasOpponent && r >= 0 && r < SIZE && c >= 0 && c < SIZE && state[r][c] == currentPlayer) {
                valid = true;
            }
        }
        return valid;
    }

    private boolean hasValidMove(int player) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (state[i][j] == 0 && checkMove(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (state[i][j] == 1) {
                    board[i][j].setText("●");
                } else if (state[i][j] == 2) {
                    board[i][j].setText("○");
                } else {
                    board[i][j].setText("");
                }
            }
        }
    }

    private void updateStatus() {
        int black = 0, white = 0;
        for (int[] row : state) {
            for (int cell : row) {
                if (cell == 1) {
                    black++;
                }
                if (cell == 2) {
                    white++;
                }
            }
        }
        String s = (currentPlayer == turn) ? "bạn" : "đối thủ";
        s += (currentPlayer == 1) ? " (●)" : " (○)";
        statusLabel.setText(String.format("Lượt của %s | Đen: %d - Trắng: %d", s, black, white));
    }

    private void showResult() {
        int black = 0, white = 0;
        for (int[] row : state) {
            for (int cell : row) {
                if (cell == 1) {
                    black++;
                }
                if (cell == 2) {
                    white++;
                }
            }
        }

        String msg = String.format("Kết thúc!\nĐen: %d - Trắng: %d\n", black, white);
        GameDTO gameDTO = new GameDTO();
        if (turn == 1) {
            gameDTO.setPlayerBlackId(user.getId());
            gameDTO.setPlayerWhiteId(0);
            gameDTO.setPlayerWinnerId(0);
        } else {
            gameDTO.setPlayerWhiteId(user.getId());
            gameDTO.setPlayerBlackId(0);
            gameDTO.setPlayerWinnerId(0);
        }
        gameDTO.setScoreBlack(black);
        gameDTO.setScoreWhite(white);
        if (black > white) {
            msg += "Người chơi Đen thắng!";
            if (turn == 1) {
                gameDTO.setPlayerWinnerId(user.getId());
            }
        } else if (white > black) {
            msg += "Người chơi Trắng thắng!";
            if (turn == 2) {
                gameDTO.setPlayerWinnerId(user.getId());
            }
        } else {
            msg += "Hòa!";
        }
        JOptionPane.showMessageDialog(this, msg);
        Message<GameDTO> message = new Message<>("sent-result-game", gameDTO);
        othelloGameController.send(message);
        new MainPlayerFrame(user);
        dispose();
    }

    private void exitGame() {
        int black = 0, white = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (state[i][j] == 1) {
                    black++;
                } else {
                    white++;
                }
            }
        }
        GameDTO gameDTO = new GameDTO();
        if (turn == 1) {
            gameDTO.setPlayerBlackId(user.getId());
        } else {
            gameDTO.setPlayerWhiteId(user.getId());
        }
        Message<GameDTO> message = new Message<>("user-exit", gameDTO);
        othelloGameController.send(message);
    }

    @Override
    public void onDataUpdated(String type, Object obj) {
        if (type.equals("update-turn")) {
            MoveDTO moveDTO = JsonUtils.convert(obj, MoveDTO.class);
            int r = moveDTO.getRow();
            int c = moveDTO.getCol();
            makeMove(r, c);
        }
        if (type.equals("user-exit")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            JOptionPane.showMessageDialog(this, "Người chơi " + userDTO.getUsername() + " đã thoát!");
            new MainPlayerFrame(user);
            dispose();
        }
    }

    @Override
    public void onConnectedToServer() {
        Message<UserDTO> message = new Message<>("new-player", user);
        othelloGameController.send(message);
    }

    @Override
    public void onDisconnectedFromServer() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onError(String message) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
