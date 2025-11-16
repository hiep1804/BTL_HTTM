/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package network;

import model.Message;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.net.URI;
import util.JsonUtils;

@ClientEndpoint
public class WebSocketClientManager {

    private Session session;
    //interface để nhận dữ liệu từ server gửi đến
    private MessageHandler handler;
    //interface để xử lí các sự kiện như connect,...
    private ConnectionListener connectionListener;

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public void connect(String uri, MessageHandler handler) {
        try {
            this.handler = handler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        if (connectionListener != null) connectionListener.onConnected();
    }

    @OnMessage
    public void onMessage(String message) {
        if (handler != null) {
            handler.onMessage(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Disconnected: " + closeReason);
    }
    public void send(Message<?> message) {
        try {
            String json = JsonUtils.toJson(message);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
