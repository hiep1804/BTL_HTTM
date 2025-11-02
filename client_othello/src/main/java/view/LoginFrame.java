/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.LoginController;
import dto.LoginBeanDTO;
import dto.UserDTO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private LoginController loginController;

    public LoginFrame() {
        setTitle("Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Đăng nhập", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelCenter = new JPanel(new GridLayout(2, 2, 10, 10));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelCenter.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField();
        panelCenter.add(txtUsername);

        panelCenter.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panelCenter.add(txtPassword);

        add(panelCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel();
        btnLogin = new JButton("Đăng nhập");

        panelBottom.add(btnLogin);
        add(panelBottom, BorderLayout.SOUTH);
        loginController=new LoginController(this);
        setVisible(true);
    }
    public void setOnLogin(ActionListener actionListener){
        btnLogin.addActionListener(actionListener);
    }
    public String getUsername() { return txtUsername.getText(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }
}
