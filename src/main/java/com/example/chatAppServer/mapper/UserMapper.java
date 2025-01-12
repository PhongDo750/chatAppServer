package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.user.ChangeInfoUserRequest;
import com.example.chatAppServer.dto.user.FriendSearchingOutput;
import com.example.chatAppServer.dto.user.UserOutputV2;
import com.example.chatAppServer.dto.user.UserRequest;
import com.example.chatAppServer.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserMapper {
    UserOutputV2 getOutputFromEntity(UserEntity userEntity);
    UserEntity getEntityFromRequest(UserRequest signUpRequest);
    void updateEntityFromInput(@MappingTarget UserEntity userEntity, ChangeInfoUserRequest changeInfoUserRequest);
    FriendSearchingOutput getFriendSearchingFrom(UserEntity userEntity);
}
