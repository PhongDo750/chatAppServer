package com.example.chatAppServer.websocket;

import com.example.chatAppServer.entity.UserChatMapEntity;
import com.example.chatAppServer.repository.UserChatRepository;
import com.example.chatAppServer.service.MessageService;
import com.example.chatAppServer.token.TokenHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WebSocketService extends TextWebSocketHandler {
    public static final Map<Long, List<WebSocketSession>> webSocketSessions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageService messageService;
    private final UserChatRepository userChatRepository;

    public WebSocketService(MessageService messageService, UserChatRepository userChatRepository) {
        this.messageService = messageService;
        this.userChatRepository = userChatRepository;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        Long userId = getUserIdBy(session);
        List<WebSocketSession> webSocketSessionOfCurrentRequest;
        if (webSocketSessions.containsKey(userId)) {
            webSocketSessionOfCurrentRequest = webSocketSessions.get(userId);
        }
        else {
            webSocketSessionOfCurrentRequest = new ArrayList<>();
        }
        webSocketSessionOfCurrentRequest.add(session);
        webSocketSessions.put(userId, webSocketSessionOfCurrentRequest);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Long userId = getUserIdBy(session);
        List<WebSocketSession> webSocketSessionsOfCurrentRequest = webSocketSessions.get(userId);
        webSocketSessionsOfCurrentRequest.remove(session);
        if (webSocketSessionsOfCurrentRequest.isEmpty()) {
            webSocketSessions.remove(userId);
        } else {
            webSocketSessions.put(userId, webSocketSessionsOfCurrentRequest);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        String payload = message.getPayload().toString();
        Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);

        TextMessage textMessage = (TextMessage) message;
        // Get the message content as a string
        String messageContent = textMessage.getPayload();
        messageService.sendMessage(messageContent, getUserIdBy(session));

        Long chatId = objectMapper.convertValue(payloadMap.get("chatId"), Long.class);
        List<Long> receiverIds = userChatRepository.findAllByChatId(chatId).stream()
                .map(UserChatMapEntity::getUserId).collect(Collectors.toList());

        for (Long receiverId : receiverIds) {
            List<WebSocketSession> receiverSession = webSocketSessions.get(receiverId);
            if (receiverSession != null) {
                // Send the message
                for (WebSocketSession webSocketSession : receiverSession) {
                    webSocketSession.sendMessage(textMessage);
                }
            }
        }
    }

    private Long getUserIdBy(WebSocketSession session){
        HttpHeaders headers = session.getHandshakeHeaders();
        return TokenHelper.getUserIdFromToken(headers.getFirst("Authorization"));
    }
}
