package view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.MainPlayerController;
import controller.UIListener;
import model.Message;
import model.User;
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
    private JDialog dialog;
    private JPanel listPanel;

    private java.util.List<Invite> invites = new ArrayList<>();
    private User user;
    private MainPlayerController mainPlayerController;
    private java.util.List<String> notifications = new CopyOnWriteArrayList<>();

    public MainPlayerFrame(User user) {
        this.user = user;
        user.setStatus(true);
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
        JLabel lblTitle = new JLabel("Danh sách người chơi online của " + user.getUsername(), SwingConstants.CENTER);
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
                Message<User> message = new Message<>("user-exit", user);
                mainPlayerController.getWebSocketClientManager().send(message);
                System.exit(0);
            }
        });
        setVisible(true);
        dialog = new JDialog(this, "Danh sách lời mời", true);
        dialog.setVisible(false);
        listPanel = new JPanel();
    }

    private void addPlayer(User user) {
        PlayerPanel p = new PlayerPanel(user);
        playerPanels.add(p);
        playerListPanel.add(p);
        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    private void sentChallengeToServer(User user) {
        Message<User> message = new Message<>("challenge", user);
        mainPlayerController.send(message);
    }
    
    private void addIntite(Invite invite){
        invites.add(invite);
        listPanel.add(invite);
        dialog.revalidate();
        dialog.repaint();
    }

    private void removeInvite(Invite invite) {
        invites.remove(invite);
        listPanel.remove(invite);
        updateInviteCount();
        dialog.revalidate();
        dialog.repaint();
    }

    private void removeAllInvites() {
        invites.clear();
        listPanel.removeAll();
        updateInviteCount();
        dialog.revalidate();
        dialog.repaint();
    }

    private void updateInviteCount() {
        int count = invites.size();
        lblInviteCount.setText(count > 0 ? "Lời mời: " + count : "");
    }

    // Hiển thị Dialog danh sách lời mời
    private void showInviteDialog() {

        dialog = new JDialog(this, "Danh sách lời mời", true);
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (Invite invite : new ArrayList<>(invites)) { // copy để tránh ConcurrentModification
            listPanel.add(invite);
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
        Message<User> message = new Message<>("accept-challenge", invite.user);
        mainPlayerController.send(message);
    }

    //gửi lời từ chối
    public void declineInvite(Invite invite, JDialog dialog) {
        removeInvite(invite);
        Message<User> message = new Message<>("decline-challenge", invite.user);
        mainPlayerController.send(message);
    }

    //từ chối tất cả
    public void declineAllInvite(JDialog dialog) {
        for (Invite invite : invites) {
            Message<User> message = new Message<>("decline-challenge", invite.user);
            mainPlayerController.send(message);
        }
        removeAllInvites();
    }

    //nhận thông báo và hiển thị
    public void showNotification() {
        if (!notifications.isEmpty()) {
            String s = notifications.get(0);
            lblNotification.setText(s);
            lblNotification.setVisible(true);
            javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
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
            User user = JsonUtils.convert(obj, User.class);
            boolean check = true;
            for (PlayerPanel playerPanel : playerPanels) {
                if (playerPanel.user.getId() == user.getId()) {
                    check = false;
                    break;
                }
            }
            if (check) {
                addPlayer(user);
            } else {
                SwingUtilities.invokeLater(() -> {

                    for (PlayerPanel player : playerPanels) {
                        if (user.getId() == player.user.getId()) {
                            player.btnChallenge.setVisible(true);
                            player.lblStatus.setText("Đang rảnh");
                        }
                    }

                    playerListPanel.revalidate();
                    playerListPanel.repaint();
                });
            }
        }
        if (type.equals("load-all-user")) {
            List<User> usersOnline = JsonUtils.convert(obj, new TypeReference<List<User>>() {
            });
            for (User userOnline : usersOnline) {
                addPlayer(userOnline);
            }
        }
        if (type.equals("challenge")) {
            User user = JsonUtils.convert(obj, User.class);
            boolean check=true;
            for(int i=0;i<invites.size();i++){
                if(invites.get(i).user.getId()==user.getId()){
                    check=false;
                }
            }
            if(check){
                Invite invite=new Invite(user);
                addIntite(invite);
                updateInviteCount();
            }
        }
        //nếu đối thủ đã thoát
        if (type.equals("user-exited")) {
            User user = JsonUtils.convert(obj, User.class);
            if (dialog.isVisible()) {
                JOptionPane.showMessageDialog(dialog, "Người chơi " + user.getUsername() + " đã thoát!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Người chơi " + user.getUsername() + " đã thoát!");
            }
        }
        //nếu là người thách đấu
        if (type.equals("accept-challenge")) {
            User user = JsonUtils.convert(obj, User.class);
            new OthelloGameFrame(user, 1);
            dispose();
        }
        //nếu là người được thách đấu
        if (type.equals("to-accept-challenge")) {
            User user = JsonUtils.convert(obj, User.class);
            new OthelloGameFrame(user, 2);
            dispose();
        }
        //đối thủ từ chối thách đấu
        if (type.equals("decline-challenge")) {
            User user = JsonUtils.convert(obj, User.class);
            notifications.add(user.getUsername() + " đã từ chối!");
            javax.swing.Timer timer = null;
            if (timer == null || timer.isRunning()) {
                timer = new javax.swing.Timer(0, e -> showNotification());
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
                    if (id1 == player.user.getId() || id2 == player.user.getId()) {
                        player.btnChallenge.setVisible(false);
                        player.lblStatus.setText("Đã vào trận");
                    }
                }
                for (Invite invite : invites) {
                    if (invite.user.getId() == id1 || invite.user.getId() == id2) {
                        JOptionPane.showMessageDialog(this, invite.user.getUsername() + " đã vào game và từ chối lời mời của bạn!");
                    }
                }
                playerListPanel.revalidate();
                playerListPanel.repaint();
            });
        }
        if (type.equals("user-exit")) {
            User user = JsonUtils.convert(obj, User.class);
            PlayerPanel p = null;
            for (int i = 0; i < playerPanels.size(); i++) {
                if (playerPanels.get(i).user.getId() == user.getId()) {
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
        Message<User> message = new Message<>("new-user", user);
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
        private JLabel lblStatus;
        private JButton btnChallenge;
        private User user;

        public PlayerPanel(User user) {
            this.user = user;
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            // Tạo panel con để chứa 2 label chồng lên nhau
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false); // giữ trong suốt nếu muốn đồng màu nền với cha

            lblName = new JLabel("  " + user.getUsername());
            lblName.setFont(new Font("Arial", Font.BOLD, 14));

            lblStatus = new JLabel("");
            lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
            lblStatus.setForeground(Color.GRAY);

            // Thêm 2 label vào infoPanel
            infoPanel.add(lblName);
            infoPanel.add(lblStatus);

            // Thêm infoPanel vào bên trái
            add(infoPanel, BorderLayout.WEST);

            // Nút "Thách đấu" ở bên phải
            btnChallenge = new JButton("Thách đấu");
            btnChallenge.setFont(new Font("Arial", Font.PLAIN, 13));
            btnChallenge.setFocusable(false);
            add(btnChallenge, BorderLayout.EAST);
            
            btnChallenge.addActionListener(e -> sentChallengeToServer(user));
            if(user.isStatus())      lblStatus.setText("Đang rảnh");
            else{
                lblStatus.setText("Đã vào trận");
                btnChallenge.setVisible(false);
            }
        }
    }

    // Lưu thông tin lời mời
    private class Invite extends JPanel {

        private JLabel lbl;
        private JButton btnAccept;
        private JButton btnDecline;

        private User user;

        public Invite(User user) {
            this.user = user;
            setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel lbl = new JLabel("Lời mời từ: " + user.getUsername());
            JButton btnAccept = new JButton("Đồng ý");
            btnAccept.addActionListener(e -> acceptInvite(this, dialog));
            JButton btnDecline = new JButton("Từ chối");
            btnDecline.addActionListener(e -> declineInvite(this, dialog));
            add(lbl);
            add(btnAccept);
            add(btnDecline);
        }
    }
}
