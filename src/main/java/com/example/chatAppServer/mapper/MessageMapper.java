package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.message.MessageInput;
import com.example.chatAppServer.entity.message.MessageEntity;
import org.mapstruct.Mapper;

@Mapper
public interface MessageMapper {
    MessageEntity getEntityFromInput(MessageInput messageInput);
}
