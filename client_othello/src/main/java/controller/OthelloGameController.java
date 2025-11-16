/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;


import model.Message;
import model.User;
import network.WebSocketClientManager;
import util.JsonUtils;
import view.MainPlayerFrame;

import model.Message;
import model.User;
import network.ConnectionListener;
import network.WebSocketClientManager;
import util.JsonUtils;

import view.OthelloGameFrame;
/**
 *
 * @author hn235
 */
public class OthelloGameController {
    private OthelloGameFrame othelloGameFrame;
    private WebSocketClientManager webSocketClientManager;

    public OthelloGameController(OthelloGameFrame othelloGameFrame) {
        this.othelloGameFrame=othelloGameFrame;
        this.webSocketClientManager = new WebSocketClientManager();
    }
    public void connect(){
        webSocketClientManager.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                System.out.println("Connected to server game!");
                othelloGameFrame.onConnectedToServer();
            }

            @Override
            public void onDisconnected() {
                
            }

            @Override
            public void onError(Throwable error) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        webSocketClientManager.connect("ws://localhost:8080/game", this::handleMessage);
    }
    private void handleMessage(String json) {
        Message<?> msg = JsonUtils.fromJson(json, Message.class);
        if(othelloGameFrame!=null){
            othelloGameFrame.onDataUpdated(msg.getType(),(Object) msg.getData());
        }
    }
    public void send(Message<?>message){
        webSocketClientManager.send(message);
    }
    public WebSocketClientManager getWebSocketClientManager() {
        return webSocketClientManager;
    }

    public void setWebSocketClientManager(WebSocketClientManager webSocketClientManager) {
        this.webSocketClientManager = webSocketClientManager;
    }
    
}

