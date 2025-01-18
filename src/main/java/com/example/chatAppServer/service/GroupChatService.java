package com.example.chatAppServer.service;

import com.example.chatAppServer.cloudinary.CloudinaryHelper;
import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.chat.*;
import com.example.chatAppServer.entity.ChatEntity;
import com.example.chatAppServer.entity.UserChatMapEntity;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.mapper.ChatMapper;
import com.example.chatAppServer.repository.ChatRepository;
import com.example.chatAppServer.repository.CustomRepository;
import com.example.chatAppServer.repository.UserChatRepository;
import com.example.chatAppServer.repository.UserRepository;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupChatService {
    private final ChatRepository chatRepository;
    private final UserChatRepository userChatRepository;
    private final CustomRepository customRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public void createGroupChat(String accessToken, CreateGroupChatInput createGroupChatInput) {
        Long managerId = TokenHelper.getUserIdFromToken(accessToken);
        if (createGroupChatInput.getUserIds().contains(managerId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        ChatEntity chatEntity = ChatEntity.builder()
                .managerId(managerId)
                .newestChatTime(LocalDateTime.now())
                .chatType(Common.GROUP)
                .name(createGroupChatInput.getName())
                .newestMessage("Create Group")
                .imageUrl(Common.IMAGE_DEFAULT)
                .build();
        chatRepository.save(chatEntity);
        userChatRepository.save(
                UserChatMapEntity.builder()
                        .userId(managerId)
                        .chatId(chatEntity.getId())
                        .build()
        );

        for (Long userId : createGroupChatInput.getUserIds()) {
            userChatRepository.save(
                    UserChatMapEntity.builder()
                            .userId(userId)
                            .chatId(chatEntity.getId())
                            .build()
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ChatMemberOutput> getGroupChatMembers(String accessToken, Long groupChatId) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        if (Boolean.FALSE.equals(userChatRepository.existsByUserIdAndChatId(userId, groupChatId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        List<UserChatMapEntity> userChatMapEntities = userChatRepository.findAllByChatId(groupChatId);
        ChatEntity chatEntity = customRepository.getChatBy(groupChatId);
        Long managerId = chatEntity.getManagerId();
        List<UserEntity> userEntities = userRepository.findAllByIdIn(
                userChatMapEntities.stream().map(UserChatMapEntity::getUserId).collect(Collectors.toList())
        );

        List<ChatMemberOutput> chatMemberOutputs = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            chatMemberOutputs.add(
                    ChatMemberOutput.builder()
                            .id(userEntity.getId())
                            .fullName(userEntity.getFullName())
                            .imageUrl(userEntity.getImageUrl())
                            .role(
                                    userEntity.getId().equals(managerId) ? Common.ADMIN : Common.MEMBER
                            )
                            .build()
            );
        }
        return chatMemberOutputs;
    }

    @Transactional
    public void addNewMemberToGroupChat(ChatAddNewMemberInput chatAddNewMemberInput, String accessToken) {
        ChatEntity chatEntity = customRepository.getChatBy(chatAddNewMemberInput.getGroupChatId());
        if (!Objects.equals(chatEntity.getManagerId(), TokenHelper.getUserIdFromToken(accessToken))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        List<UserChatMapEntity> userChatMapEntities =
                userChatRepository.findAllByChatId(chatAddNewMemberInput.getGroupChatId());

        List<Long> userIdsInGroup = userChatMapEntities.stream()
                .map(UserChatMapEntity::getUserId)
                .collect(Collectors.toList());

        for (Long newUserId : chatAddNewMemberInput.getUserIds()) {
            if (!userIdsInGroup.contains(newUserId)) {
                userChatRepository.save(
                        UserChatMapEntity.builder()
                                .chatId(chatAddNewMemberInput.getGroupChatId())
                                .userId(newUserId)
                                .build()
                );
            } else {
                throw new RuntimeException(Common.ACTION_FAIL);
            }
        }
    }

    @Transactional
    public void deleteMember(String accessToken, ChatDeleteMemberInput chatDeleteMemberInput) {
        ChatEntity chatEntity = customRepository.getChatBy(chatDeleteMemberInput.getGroupChatId());
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        if (!Objects.equals(chatEntity.getManagerId(), userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Objects.equals(chatDeleteMemberInput.getUserId(), userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        userChatRepository.deleteByUserIdAndChatId(
                chatDeleteMemberInput.getUserId(),
                chatDeleteMemberInput.getGroupChatId()
        );
    }

    @Transactional
    public void leaveTheGroupChat(String accessToken, Long chatId) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        if (userChatRepository.countByChatId(chatId) > 1) {
            userChatRepository.deleteByUserIdAndChatId(
                    userId,
                    chatId
            );
        } else {
            userChatRepository.deleteByUserIdAndChatId(
                    userId,
                    chatId
            );
            chatRepository.deleteById(chatId);
        }
    }

    @Transactional(readOnly = true)
    public Page<GroupChatOutPut> getGroups(String accessToken, String search, Pageable pageable){
        // lay map => lay chatId
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        List<Long> chatIds = userChatRepository.findAllByUserId(userId).stream().map(
                UserChatMapEntity::getChatId
        ).collect(Collectors.toList());

        Page<ChatEntity> groupChatEntities = Page.empty();
        if (Objects.isNull(search)){
            groupChatEntities = chatRepository.findAllByIdIn(chatIds, pageable);
        } else{
            groupChatEntities = chatRepository.findAllByNameContainingIgnoreCaseAndIdIn(search, chatIds, pageable);
        }

        return groupChatEntities.map(chatEntity -> {
            GroupChatOutPut groupChatOutPut = chatMapper.getGroupChatOutputFromEntity(chatEntity);
            groupChatOutPut.setImg(Common.IMAGE_DEFAULT);
            return groupChatOutPut;
        });
    }
}
