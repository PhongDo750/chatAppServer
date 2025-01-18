package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.message.MessageInput;
import com.example.chatAppServer.entity.ChatEntity;
import com.example.chatAppServer.entity.UserChatMapEntity;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.entity.message.EventNotificationEntity;
import com.example.chatAppServer.entity.message.MessageEntity;
import com.example.chatAppServer.mapper.MessageMapper;
import com.example.chatAppServer.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final EventNotificationRepository eventNotificationRepository;
    private final UserChatRepository userChatRepository;
    private final CustomRepository customRepository;
    private final ChatRepository chatRepository;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public String sendMessage(String messageJson, Long userId) throws JsonProcessingException {
        MessageInput messageInput = objectMapper.readValue(messageJson, MessageInput.class);
        LocalDateTime now = LocalDateTime.now();
        Long senderId = userId;
        UserEntity sender = customRepository.getUserBy(senderId);
        ChatEntity chatEntity = customRepository.getChatBy(messageInput.getChatId());
        chatEntity.setNewestUserId(senderId);
        chatEntity.setNewestMessage(messageInput.getMessage());
        chatEntity.setNewestChatTime(now);

        MessageEntity messageEntity = messageMapper.getEntityFromInput(messageInput);
        messageEntity.setSenderId(senderId);
        messageEntity.setCreateAt(LocalDateTime.now());
        Long chatId2;
        if (chatEntity.getChatType().equals(Common.USER)) {
            ChatEntity chatEntity2 = chatRepository.findByUserId1AndUserId2(chatEntity.getUserId2(), chatEntity.getUserId1());
            chatId2 = chatEntity2.getId();
            messageEntity.setChatId1(chatEntity.getId());
            messageEntity.setChatId2(chatEntity2.getId());
            chatEntity2.setNewestMessage(messageInput.getMessage());
            chatEntity2.setNewestUserId(senderId);
            chatEntity2.setNewestChatTime(now);
            chatRepository.save(chatEntity2);
        } else {
            chatId2 = null;
            messageEntity.setGroupChatId(chatEntity.getId());
        }
        messageRepository.save(messageEntity);
        CompletableFuture.runAsync(() -> {
            chatRepository.save(chatEntity);

            // if chat user-user
            if (chatEntity.getChatType().equals(Common.USER)) {
                eventNotificationRepository.save(
                        EventNotificationEntity.builder()
                                .eventType(Common.MESSAGE)
                                .userId(chatEntity.getUserId2())
                                .state(Common.NEW_EVENT)
                                .chatId(chatId2)
                                .build()
                );
                pushNotificationService.sendNotification(chatEntity.getUserId2(), Common.MESSAGE);
            } else { // if chat user-group
                List<UserChatMapEntity> userChatEntities = userChatRepository.findAllByChatId(messageInput.getChatId()).stream()
                        .filter(userChatEntity -> !userChatEntity.getUserId().equals(senderId))
                        .collect(Collectors.toList());
                if (!userChatEntities.isEmpty()) {
                    for (UserChatMapEntity userChatEntity : userChatEntities) {
                        eventNotificationRepository.save(
                                EventNotificationEntity.builder()
                                        .eventType(Common.MESSAGE)
                                        .userId(userChatEntity.getUserId())
                                        .state(Common.NEW_EVENT)
                                        .chatId(chatEntity.getId())
                                        .build()
                        );
                        pushNotificationService.sendNotification(userChatEntity.getUserId(), Common.MESSAGE);
                    }
                }
            }
        });
        return messageInput.getMessage();
    }
}
