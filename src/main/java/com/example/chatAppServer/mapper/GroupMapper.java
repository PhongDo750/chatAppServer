package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.group.ChangeInformationGroupInput;
import com.example.chatAppServer.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface GroupMapper {
    void updateGroupFromInput(@MappingTarget GroupEntity groupEntity, ChangeInformationGroupInput changeInformationGroupInput);
}
