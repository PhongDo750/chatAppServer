package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.chat.ChatOutput;
import com.example.chatAppServer.dto.message.MessageOutput;
import com.example.chatAppServer.entity.ChatEntity;
import com.example.chatAppServer.entity.UserChatMapEntity;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.entity.message.EventNotificationEntity;
import com.example.chatAppServer.entity.message.MessageEntity;
import com.example.chatAppServer.repository.*;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final CustomRepository customRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EventNotificationRepository eventNotificationRepository;
    private final UserChatRepository userChatRepository;

    @Transactional(readOnly = true)
    public Page<ChatOutput> getChatList(String accessToken, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        List<Long> chatIds = userChatRepository.findAllByUserId(userId).stream().map(
                UserChatMapEntity::getChatId
        ).collect(Collectors.toList());

        Page<ChatEntity> chatEntityPage = chatRepository.findAllByIdIn(chatIds, pageable);
        if (Objects.isNull(chatEntityPage) || chatEntityPage.isEmpty()) {
            return Page.empty();
        }

        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(
                chatEntityPage.stream().map(ChatEntity::getUserId2).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        Map<Long, List<EventNotificationEntity>> eventNotificationMap =
                eventNotificationRepository.findAllByUserIdAndEventType(userId, Common.MESSAGE).stream()
                        .collect(Collectors.groupingBy(EventNotificationEntity::getChatId));


        return chatEntityPage.map(
                chatEntity -> {
                    ChatOutput chatOutput = new ChatOutput();
                    if (chatEntity.getChatType().equals(Common.USER)) {
                        UserEntity userEntity = userEntityMap.get(chatEntity.getUserId2());
                        chatOutput = ChatOutput.builder()
                                .id(chatEntity.getId())
                                .name(userEntity.getFullName())
                                .imageUrl(userEntity.getImageUrl())
                                .newestMessage(chatEntity.getNewestMessage())
                                .newestChatTime(chatEntity.getNewestChatTime())
                                .build();
                    } else {
                        chatOutput = ChatOutput.builder()
                                .id(chatEntity.getId())
                                .name(chatEntity.getName())
                                .imageUrl(chatEntity.getImageUrl())
                                .newestMessage(chatEntity.getNewestMessage())
                                .newestChatTime(chatEntity.getNewestChatTime())
                                .build();
                    }
                    if (eventNotificationMap.containsKey(chatOutput.getId())) {
                        chatOutput.setMessageCount(eventNotificationMap.get(chatOutput.getId()).size());
                    } else {
                        chatOutput.setMessageCount(0);
                    }
                    chatOutput.setIsMe(userId.equals(chatEntity.getNewestUserId()));
                    return chatOutput;
                }
        );
    }

    @Transactional
    public Page<MessageOutput> getMessages(String accessToken, Long chatId, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        ChatEntity chatEntity = customRepository.getChatBy(chatId);
        eventNotificationRepository.deleteAllByChatIdAndUserId(chatId, userId);
        Page<MessageEntity> messageEntityPage = messageRepository.searchAllByChatId(chatId, pageable);
        if (Objects.isNull(messageEntityPage) || messageEntityPage.isEmpty()) {
            return Page.empty();
        }

        List<Long> userIds = new ArrayList<>();
        if (chatEntity.getChatType().equals(Common.USER)) {
            userIds.add(chatEntity.getUserId2());
            userIds.add(chatEntity.getUserId1());
        } else {
            userIds = userChatRepository.findAllByChatId(chatId).stream()
                    .map(UserChatMapEntity::getUserId).collect(Collectors.toList());
        }
        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(userIds)
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return messageEntityPage.map(
                messageEntity -> {
                    UserEntity userEntity = userEntityMap.get(messageEntity.getSenderId());
                    MessageOutput messageOutput = new MessageOutput();
                    messageOutput.setUserId(userEntity.getId());
                    messageOutput.setMessage(messageEntity.getMessage());
                    messageOutput.setFullName(userEntity.getFullName());
                    messageOutput.setImageUrl(userEntity.getImageUrl());
                    messageOutput.setCreatedAt(messageEntity.getCreateAt());
                    messageOutput.setIsMe(
                            userId.equals(messageEntity.getSenderId()) ? Boolean.TRUE : Boolean.FALSE
                    );
                    return messageOutput;
                }
        );
    }
}
