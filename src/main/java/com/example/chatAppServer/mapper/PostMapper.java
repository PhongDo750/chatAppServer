package com.example.chatAppServer.mapper;

import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.entity.PostEntity;
import org.mapstruct.Mapper;

@Mapper
public interface PostMapper {
    PostOutput getOutputFromEntity(PostEntity postEntity);
}
