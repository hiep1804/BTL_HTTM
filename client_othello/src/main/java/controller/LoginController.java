/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dto.LoginBeanDTO;
import dto.UserDTO;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import util.JsonUtils;
import view.LoginFrame;
import view.MainPlayerFrame;

/**
 *
 * @author hn235
 */
public class LoginController {
    private LoginFrame loginFrame;
    public LoginController(LoginFrame loginFrame){
        this.loginFrame=loginFrame;
        loginFrame.setOnLogin(e->{
            String username = loginFrame.getUsername();
            String password = loginFrame.getPassword();
            if (username.isEmpty() || password.isEmpty() || username.trim().equals("") || password.trim().equals("")) {
                JOptionPane.showMessageDialog(loginFrame, "Vui lòng nhập đủ thông tin!");
                return;
            }
            LoginBeanDTO loginBeanDTO = new LoginBeanDTO();
            loginBeanDTO.setUsername(username);
            loginBeanDTO.setPassword(password);
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginBeanDTO.objectToJSON()))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode()==200){
                    //hiển thị thông báo
                    JOptionPane.showMessageDialog(loginFrame, "Đăng nhập thành công");
                    //chuyển chuỗi json thành đối tượng
                    UserDTO userDTO=JsonUtils.fromJson(response.body(), UserDTO.class);
                    new MainPlayerFrame(userDTO);
                    loginFrame.dispose();
                }
                else{
                    JOptionPane.showMessageDialog(loginFrame, response.body());
                }
            } catch (IOException ex) {
                Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}