package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.chat.GroupChatOutPut;
import com.example.chatAppServer.entity.ChatEntity;
import org.mapstruct.Mapper;

@Mapper
public interface ChatMapper {
    GroupChatOutPut getGroupChatOutputFromEntity(ChatEntity chatEntity);
}
