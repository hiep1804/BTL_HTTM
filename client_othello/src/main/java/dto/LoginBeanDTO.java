/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author hn235
 */
public class LoginBeanDTO {

    private String username;
    private String password;

    public LoginBeanDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginBeanDTO{" + "username=" + username + ", password=" + password + '}';
    }

    public String objectToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData="";
        try {
            // Tự động chuyển đổi đối tượng thành chuỗi JSON
            jsonData = mapper.writeValueAsString(this);
            System.out.println(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonData;
    }
}
