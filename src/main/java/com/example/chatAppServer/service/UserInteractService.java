package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.post.CommentOutput;
import com.example.chatAppServer.dto.post.PostInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.dto.user.UserOutput;
import com.example.chatAppServer.entity.*;
import com.example.chatAppServer.entity.message.EventNotificationEntity;
import com.example.chatAppServer.helper.StringUtils;
import com.example.chatAppServer.mapper.PostMapper;
import com.example.chatAppServer.repository.*;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserInteractService {
    private final LikeMapRepository likeMapRepository;
    private final CommentRepository commentRepository;
    private final CustomRepository customRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final EventNotificationRepository eventNotificationRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void likePost(Long postId, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = customRepository.getUserBy(userId);
        if (Boolean.TRUE.equals(likeMapRepository.existsByUserIdAndPostId(postId, userId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        PostEntity postEntity = customRepository.getPostBy(postId);
        CompletableFuture.runAsync(() -> {
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(Common.USER)
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.LIKE)
                            .postId(postId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            likeMapRepository.save(
                    LikeMapEntity.builder()
                            .userId(userId)
                            .postId(postId)
                            .build()
            );
            eventNotificationRepository.save(
                    EventNotificationEntity.builder()
                            .eventType(Common.NOTIFICATION)
                            .userId(postEntity.getUserId())
                            .state(Common.NEW_EVENT)
                            .build()
            );

            //push notification
            pushNotificationService.sendNotification(postEntity.getUserId(), Common.LIKE);
        });
    }

    @Transactional
    public void deleteLike(Long postId, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        likeMapRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Transactional
    public void commentPost(Long postId, String accessToken, String comment, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = customRepository.getUserBy(userId);
        PostEntity postEntity = customRepository.getPostBy(postId);
        CommentEntity commentEntity = CommentEntity.builder()
                .userId(userId)
                .postId(postId)
                .comment(comment)
                .imageUrl(
                        Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
                )
                .createAt(LocalDateTime.now())
                .build();
        commentRepository.save(commentEntity);

        CompletableFuture.runAsync(() -> {
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(Common.USER)
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.COMMENT)
                            .postId(postId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            eventNotificationRepository.save(
                    EventNotificationEntity.builder()
                            .eventType(Common.NOTIFICATION)
                            .userId(postEntity.getUserId())
                            .state(Common.NEW_EVENT)
                            .build()
            );

            //push notification
            pushNotificationService.sendNotification(postEntity.getUserId(), Common.COMMENT);
        });
    }

    @Transactional
    public void updateComment(Long commentId, String accessToken, String comment, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        CommentEntity commentEntity = customRepository.getCommentBy(commentId);
        if (Objects.isNull(commentEntity) || !commentEntity.getUserId().equals(userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        commentEntity.setComment(comment);
        commentEntity.setImageUrl(
                Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
        );
        commentRepository.save(commentEntity);
    }

    @Transactional
    public void deleteComment(Long commentId, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        CommentEntity commentEntity = customRepository.getCommentBy(commentId);
        if (!commentEntity.getUserId().equals(userId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        commentRepository.delete(commentEntity);
    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getUsersLikeOfPost(Long postId, Pageable pageable) {
        Page<LikeMapEntity> likeMapEntities = likeMapRepository.findAllByPostId(postId, pageable);
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()) {
            return Page.empty();
        }
        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(
                likeMapEntities.stream().map(LikeMapEntity::getUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return likeMapEntities.map(
                likeMapEntity -> {
                    UserEntity userEntity = userEntityMap.get(likeMapEntity.getUserId());
                    return UserOutput.builder()
                            .fullName(userEntity.getFullName())
                            .imageUrl(userEntity.getImageUrl())
                            .id(userEntity.getId())
                            .build();
                }
        );
    }

    @Transactional(readOnly = true)
    public PostOutput getPostAndComment(Long postId, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPostBy(postId);
        UserEntity userEntity = customRepository.getUserBy(postEntity.getUserId());
        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
        postOutput.setHasLike(likeMapRepository.existsByUserIdAndPostId(userId, postId));
        postOutput.setFullName(userEntity.getFullName());
        postOutput.setImageUrl(userEntity.getImageUrl());
        postOutput.setImageUrls(
                Objects.isNull(postEntity.getImageUrlsString()) ? null : StringUtils.getListFromString(postEntity.getImageUrlsString())
        );
        postOutput.setLikeCount(likeMapRepository.countAllByPostId(postId));
        postOutput.setCommentCount(commentRepository.countAllByPostId(postId));
        postOutput.setShareCount(postRepository.countAllByShareId(postId));
        if (Objects.nonNull(postEntity.getShareId())) {
            PostEntity sharedPostEntity = customRepository.getPostBy(postEntity.getShareId());
            UserEntity sharedUserEntity = customRepository.getUserBy(postEntity.getUserId());
            PostOutput sharedPostOutput = postMapper.getOutputFromEntity(sharedPostEntity);
            sharedPostOutput.setFullName(sharedUserEntity.getFullName());
            sharedPostOutput.setImageUrl(sharedUserEntity.getImageUrl());
            sharedPostOutput.setImageUrls(
                    Objects.isNull(sharedPostEntity.getImageUrlsString()) ? null : StringUtils.getListFromString(sharedPostEntity.getImageUrlsString())
            );
            postOutput.setSharePost(sharedPostOutput);
        }

        List<CommentEntity> commentEntities = commentRepository.findAllByPostId(postId);
        if (Objects.isNull(commentEntities) || commentEntities.isEmpty()) {
            return postOutput;
        }

        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(
                commentEntities.stream().map(CommentEntity::getUserId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        List<CommentOutput> commentOutputs = new ArrayList<>();
        for (CommentEntity commentMapEntity : commentEntities) {
            UserEntity commentUser = userEntityMap.get(commentMapEntity.getUserId());
            commentOutputs.add(
                    CommentOutput.builder()
                            .id(commentMapEntity.getId())
                            .postId(commentMapEntity.getPostId())
                            .userId(commentMapEntity.getUserId())
                            .comment(commentMapEntity.getComment())
                            .createdAt(commentMapEntity.getCreateAt())
                            .fullName(commentUser.getFullName())
                            .imageUrl(commentUser.getImageUrl())
                            .canDelete(userId.equals(commentUser.getId()))
                            .build()
            );
        }
        postOutput.setComments(commentOutputs);
        return postOutput;
    }

    @Transactional
    public void sharePost(String accessToken, Long shareId, PostInput sharePostInput) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPostBy(shareId);

        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        PostEntity sharePostEntity = PostEntity.builder()
                .userId(userId)
                .content(sharePostInput.getContent())
                .state(sharePostInput.getState())
                .type(sharePostInput.getType())
                .createdAt(LocalDateTime.now())
                .shareId(shareId)
                .groupId(
                        Objects.isNull(sharePostInput.getGroupId()) ? null : sharePostInput.getGroupId()
                )
                .build();
        postRepository.save(sharePostEntity);

        CompletableFuture.runAsync(() -> {
            notificationRepository.save(
                    NotificationEntity.builder()
                            .type(sharePostInput.getType())
                            .userId(postEntity.getUserId())
                            .interactId(userId)
                            .interactType(Common.SHARE)
                            .postId(shareId)
                            .hasSeen(false)
                            .createdAt(LocalDateTime.now())
                            .groupId(
                                    Objects.isNull(sharePostInput.getGroupId()) ? null : sharePostInput.getGroupId()
                            )
                            .build()
            );

            //push notification
            pushNotificationService.sendNotification(postEntity.getUserId(), "Thông báo");
        });
    }
}
