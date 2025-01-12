package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.event.NotificationOutput;
import com.example.chatAppServer.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface NotificationMapper {
    NotificationOutput getOutputFromEntity(NotificationEntity notificationEntity);
}
