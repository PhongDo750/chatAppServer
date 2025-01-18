package com.example.chatAppServer.repository;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.entity.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CustomRepository {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChatRepository chatRepository;

    public UserEntity getUserBy(Long userId){
        return userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }

    public GroupEntity getGroupBy(Long groupId){
        return groupRepository.findById(groupId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }

    public PostEntity getPostBy(Long postId){
        return postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }

    public CommentEntity getCommentBy(Long commentId){
        return commentRepository.findById(commentId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }

    public ChatEntity getChatBy(Long chatId){
        return chatRepository.findById(chatId).orElseThrow(
                () -> new RuntimeException(Common.RECORD_NOT_FOUND)
        );
    }
}
