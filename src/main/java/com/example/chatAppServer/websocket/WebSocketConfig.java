package com.example.chatAppServer.websocket;

import com.example.chatAppServer.repository.UserChatRepository;
import com.example.chatAppServer.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final MessageService messageService;
    private final UserChatRepository userChatRepository;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(new WebSocketService(messageService, userChatRepository), "/chat").setAllowedOrigins("*");
    }
}
