package com.example.server_othello.configuration;

import com.example.server_othello.network.GameSocketHandler;
import com.example.server_othello.network.MainSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MainSocketHandler mainSocketHandler;
    @Autowired
    private GameSocketHandler gameSocketHandler;
    public WebSocketConfig(MainSocketHandler mainSocketHandler) {
        this.mainSocketHandler = mainSocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mainSocketHandler, "/online").setAllowedOrigins("*");
        registry.addHandler(gameSocketHandler, "/game").setAllowedOrigins("*");
    }
}
//spring.datasource.url=jdbc:mysql://localhost:3306/game_othello
//spring.datasource.username=root
//spring.datasource.password=12345
//spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
//spring.jpa.database-platform = org.hibernate.dialect.MySQLDialect
//spring.jpa.generate-ddl=true
//spring.jpa.hibernate.ddl-auto = update
