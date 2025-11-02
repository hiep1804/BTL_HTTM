/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dto.Message;
import dto.UserDTO;
import network.ConnectionListener;
import network.MessageHandler;
import network.WebSocketClientManager;
import util.JsonUtils;
import view.MainPlayerFrame;

/**
 *
 * @author hn235
 */
public class MainPlayerController {

    private MainPlayerFrame mainPlayerFrame;
    private WebSocketClientManager webSocketClientManager;
    //check xem có cần tạo 1 user vô server mới ko
    private boolean status;

    public MainPlayerController() {
        this.webSocketClientManager = new WebSocketClientManager();
        status = true;
    }

    public MainPlayerController(MainPlayerFrame mainPlayerFrame) {
        this.mainPlayerFrame = mainPlayerFrame;
        this.webSocketClientManager = new WebSocketClientManager();
        status = true;
    }

    public void connect() {
        webSocketClientManager.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                System.out.println("Connected to server online!");
                if (status) {
                    mainPlayerFrame.onConnectedToServer();
                }
            }

            @Override
            public void onDisconnected() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public void onError(Throwable error) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        webSocketClientManager.connect("ws://localhost:8080/online", new MessageHandler() {
            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }
        });
    }

    private void handleMessage(String json) {
        Message<?> msg = JsonUtils.fromJson(json, Message.class);
        if (mainPlayerFrame != null) {
            mainPlayerFrame.onDataUpdated(msg.getType(), (Object) msg.getData());
        }
    }

    public void send(Message<?> message) {
        webSocketClientManager.send(message);
    }

    public WebSocketClientManager getWebSocketClientManager() {
        return webSocketClientManager;
    }

    public void setWebSocketClientManager(WebSocketClientManager webSocketClientManager) {
        this.webSocketClientManager = webSocketClientManager;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
