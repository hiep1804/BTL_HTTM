package view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.MainPlayerController;
import controller.UIListener;
import dto.Message;
import dto.UserDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.WebSocketClientManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import util.JsonUtils;

public class MainPlayerFrame extends JFrame implements UIListener {

    private JPanel playerListPanel;
    private java.util.List<PlayerPanel> playerPanels = new ArrayList<>();

    private JButton btnInvites;
    private JLabel lblInviteCount;
    private JLabel lblNotification;

    private java.util.List<Invite> invites = new ArrayList<>();
    private UserDTO user;
    private MainPlayerController mainPlayerController;
    private java.util.List<String> notifications = new CopyOnWriteArrayList<>();

    public MainPlayerFrame(UserDTO user) {
        this.user = user;
        initUI();
        mainPlayerController = new MainPlayerController(this);
        mainPlayerController.connect();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        //Thông báo
        lblNotification = new JLabel("", SwingConstants.CENTER);
        lblNotification.setOpaque(true);
        lblNotification.setBackground(new Color(0, 0, 0, 170)); // nền đen trong suốt
        lblNotification.setForeground(Color.WHITE);
        lblNotification.setBounds(50, 30, 300, 40);
        lblNotification.setVisible(false); 
        add(lblNotification);

        // Tiêu đề
        JLabel lblTitle = new JLabel("Danh sách người chơi online", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Danh sách người chơi
        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(playerListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Người chơi trực tuyến"));
        add(scrollPane, BorderLayout.CENTER);

        // Nhãn hiển thị số lượng lời mời
        lblInviteCount = new JLabel("", SwingConstants.CENTER);
        lblInviteCount.setFont(new Font("Arial", Font.BOLD, 14));

        // Nút lời mời
        btnInvites = new JButton("Lời mời");
        btnInvites.setFocusable(false);
        btnInvites.addActionListener(e -> showInviteDialog());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(lblInviteCount, BorderLayout.NORTH);
        bottomPanel.add(btnInvites, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Message<UserDTO> message = new Message<>("user-exit", user);
                mainPlayerController.getWebSocketClientManager().send(message);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private void addPlayer(UserDTO user) {
        PlayerPanel p = new PlayerPanel(user);
        playerPanels.add(p);
        playerListPanel.add(p);
        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    private void sentChallengeToServer(UserDTO user) {
        Message<UserDTO> message = new Message<>("challenge", user);
        mainPlayerController.send(message);
    }

    private void removeInvite(Invite invite) {
        invites.remove(invite);
        updateInviteCount();
    }

    private void removeAllInvites() {
        invites.clear();
        updateInviteCount();
    }

    private void updateInviteCount() {
        int count = invites.size();
        lblInviteCount.setText(count > 0 ? "Lời mời: " + count : "");
    }

    // Hiển thị Dialog danh sách lời mời
    private void showInviteDialog() {
        if (invites.isEmpty()) {
            return;
        }

        JDialog dialog = new JDialog(this, "Danh sách lời mời", true);
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));

        // Panel danh sách lời mời
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (Invite invite : new ArrayList<>(invites)) { // copy để tránh ConcurrentModification
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel lbl = new JLabel("Lời mời từ: " + invite.user.getUsername());
            JButton btnAccept = new JButton("Đồng ý");
            btnAccept.addActionListener(e -> acceptInvite(invite, dialog));
            JButton btnDecline = new JButton("Từ chối");
            btnDecline.addActionListener(e -> declineInvite(invite, dialog));
            p.add(lbl);
            p.add(btnAccept);
            p.add(btnDecline);
            listPanel.add(p);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Nút Từ chối tất cả
        JButton btnDeclineAll = new JButton("Từ chối tất cả");
        btnDeclineAll.addActionListener(e -> {
            declineAllInvite(dialog);
        });
        dialog.add(btnDeclineAll, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    //gửi lời đồng ý
    public void acceptInvite(Invite invite, JDialog dialog) {
        removeInvite(invite);
        dialog.dispose();
        Message<UserDTO> message = new Message<>("accept-challenge", invite.user);
        mainPlayerController.send(message);
    }

    //gửi lời từ chối
    public void declineInvite(Invite invite, JDialog dialog) {
        removeInvite(invite);
        dialog.dispose();
        Message<UserDTO> message = new Message<>("decline-challenge", invite.user);
        mainPlayerController.send(message);
        showInviteDialog();
    }

    //từ chối tất cả
    public void declineAllInvite(JDialog dialog) {
        for (Invite invite : invites) {
            Message<UserDTO> message = new Message<>("decline-challenge", invite.user);
            mainPlayerController.send(message);
        }
        removeAllInvites();
        dialog.dispose();
    }

    //nhận thông báo và hiển thị
    public void showNotification() {
        if (!notifications.isEmpty()) {
            String s = notifications.get(0);
            lblNotification.setText(s);
            lblNotification.setVisible(true);
            javax.swing.Timer timer=new javax.swing.Timer(1000, e->{
                lblNotification.setVisible(false);
                notifications.remove(0);
                showNotification();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    @Override
    public void onDataUpdated(String type, Object obj) {
        if (type.equals("add-user")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            boolean check = true;
            for (PlayerPanel playerPanel : playerPanels) {
                if (playerPanel.userDTO.getId() == userDTO.getId()) {
                    check = false;
                    break;
                }
            }
            if (check) {
                addPlayer(userDTO);
            } else {
                SwingUtilities.invokeLater(() -> {

                    for (PlayerPanel player : playerPanels) {
                        if (userDTO.getId() == player.userDTO.getId()) {
                            player.btnChallenge.setEnabled(true);
                        }
                    }

                    playerListPanel.revalidate();
                    playerListPanel.repaint();
                });
            }
        }
        if (type.equals("load-all-user")) {
            List<UserDTO> usersOnline = JsonUtils.convert(obj, new TypeReference<List<UserDTO>>() {
            });
            for (UserDTO userOnline : usersOnline) {
                addPlayer(userOnline);
            }
        }
        if (type.equals("challenge")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            invites.add(new Invite(userDTO));
            updateInviteCount();
        }
        //nếu là người thách đấu
        if (type.equals("accept-challenge")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            new OthelloGameFrame(user, 1);
            dispose();
        }
        //nếu là người được thách đấu
        if (type.equals("to-accept-challenge")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            new OthelloGameFrame(user, 2);
            dispose();
        }
        //đối thủ từ chối thách đấu
        if (type.equals("decline-challenge")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            notifications.add(userDTO.getUsername() + " đã từ chối!");
            javax.swing.Timer timer = null;
            if(timer==null||timer.isRunning()){
                timer=new javax.swing.Timer(0, e->showNotification());
                timer.setRepeats(false);
                timer.start();
            }
        }
        //thiết lập lại trạng thái người chơi khi có người tạo ván đấu
        if (type.equals("update-status")) {
            SwingUtilities.invokeLater(() -> {
                String ids = (String) obj;
                String[] tmp = ids.split(" ");
                int id1 = Integer.parseInt(tmp[0]);
                int id2 = Integer.parseInt(tmp[1]);

                for (PlayerPanel player : playerPanels) {
                    if (id1 == player.userDTO.getId() || id2 == player.userDTO.getId()) {
                        player.btnChallenge.setEnabled(false);
                    }
                }

                playerListPanel.revalidate();
                playerListPanel.repaint();
            });
        }
        if (type.equals("user-exit")) {
            UserDTO userDTO = JsonUtils.convert(obj, UserDTO.class);
            PlayerPanel p = null;
            for (int i = 0; i < playerPanels.size(); i++) {
                if (playerPanels.get(i).userDTO.getId() == userDTO.getId()) {
                    p = playerPanels.get(i);
                    playerPanels.remove(i);
                }
            }
            if (p != null) {
                playerListPanel.remove(p);
                playerListPanel.revalidate();
                playerListPanel.repaint();
            }
        }
    }

    @Override
    public void onConnectedToServer() {
        Message<UserDTO> message = new Message<>("new-user", user);
        mainPlayerController.send(message);
    }

    @Override
    public void onDisconnectedFromServer() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onError(String message) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private class PlayerPanel extends JPanel {

        private JLabel lblName;
        private JButton btnChallenge;
        private UserDTO userDTO;

        public PlayerPanel(UserDTO user) {
            this.userDTO = user;
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            lblName = new JLabel("  " + user.getUsername());
            lblName.setFont(new Font("Arial", Font.PLAIN, 14));
            add(lblName, BorderLayout.WEST);

            btnChallenge = new JButton("Thách đấu");
            btnChallenge.setFont(new Font("Arial", Font.PLAIN, 13));
            btnChallenge.setFocusable(false);
            add(btnChallenge, BorderLayout.EAST);
            btnChallenge.addActionListener(e -> sentChallengeToServer(user));
        }
    }

    // Lưu thông tin lời mời
    private static class Invite {

        UserDTO user;

        public Invite(UserDTO user) {
            this.user = user;
        }
    }
}
