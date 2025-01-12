package com.example.chatAppServer.service;

import com.example.chatAppServer.cloudinary.CloudinaryHelper;
import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.post.PostGroupInput;
import com.example.chatAppServer.dto.post.PostInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.entity.*;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PostGroupService {
    private final PostRepository postRepository;
    private final CustomRepository customRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final LikeMapRepository likeMapRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createPost(String accessToken, PostGroupInput postGroupInput, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = PostEntity.builder()
                .userId(userId)
                .content(postGroupInput.getContent())
                .state(Common.PUBLIC)
                .type(Common.GROUP)
                .imageUrlsString(
                        Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
                )
                .createdAt(LocalDateTime.now())
                .groupId(postGroupInput.getGroupId())
                .build();
        postRepository.save(postEntity);
    }

    @Transactional
    public void updatePost(String accessToken, Long postId, PostGroupInput updatePostGroupInput, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPostBy(postId);
        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        postEntity.setContent(updatePostGroupInput.getContent());
        postEntity.setImageUrlsString(
                Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
        );
        postRepository.save(postEntity);
    }

    @Transactional
    public void deletePost(String accessToken, Long postId, Long groupId) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Long adminId = userGroupRepository.findByRoleAndGroupId(Common.ADMIN, groupId).getUserId();
        PostEntity postEntity = customRepository.getPostBy(postId);
        if (!userId.equals(postEntity.getUserId()) || !userId.equals(adminId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        postRepository.delete(postEntity);
    }

    @Transactional
    public void sharePost(String accessToken, Long shareId, PostInput sharePostInput) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPostBy(shareId);

        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

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
                            .build()
            );


            //push notification
        });

        PostEntity sharePostEntity = PostEntity.builder()
                .userId(userId)
                .content(sharePostInput.getContent())
                .state(sharePostInput.getState())
                .type(sharePostInput.getType())
                .createdAt(LocalDateTime.now())
                .shareId(shareId)
                .build();
        postRepository.save(sharePostEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostGroup(String accessToken, Long groupId, Pageable pageable) {
        Page<PostEntity> postEntityPage = postRepository.findAllByGroupIdAndType(groupId,Common.GROUP, pageable);
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        List<Long> userIds = userGroupRepository.findAllByGroupId(groupId)
                .stream().map(UserGroupEntity::getUserId).collect(Collectors.toList());
        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(userIds).stream().collect(Collectors.toMap(
                UserEntity::getId, Function.identity()
        ));
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        UserEntity userEntity = customRepository.getUserBy(userId);
        return setHasLikeForPosts(userId, mapResponsePostPage(postEntityPage, userEntityMap));
    }

    private Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserEntity> userEntityMap) {
        List<Long> shareIds = new ArrayList<>(); // 2 share tu 1, bai 3 share tu bai 1
        for (PostEntity postEntity : postEntityPage) {
            if (Objects.nonNull(postEntity.getShareId())) {
                shareIds.add(postEntity.getShareId());
            }
        }
        Map<Long, PostOutput> sharePostOutputMap;
        if (!shareIds.isEmpty()) {
            List<PostEntity> sharePostEntities = postRepository.findAllByIdIn(shareIds);

            List<PostOutput> sharePostOutputs = sharePostEntities.stream() // entity
                    .map(postEntity -> {
                        PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                        postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
                        return postOutput;
                    }) // output
                    .collect(Collectors.toList());

            sharePostOutputMap = sharePostOutputs.stream().collect(Collectors.toMap(PostOutput::getId, Function.identity()));

            List<Long> shareUserIds = sharePostOutputs.stream()
                    .map(PostOutput::getUserId)
                    .collect(Collectors.toList());

            Map<Long, UserEntity> shareUserEntiyMap = userRepository.findAllByIdIn(shareUserIds).stream()
                    .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

            sharePostOutputs.stream().map(
                    postOutput -> {
                        UserEntity user = shareUserEntiyMap.get(postOutput.getUserId());
                        postOutput.setImageUrl(user.getImageUrl());
                        postOutput.setFullName(user.getFullName());
                        return postOutput;
                    }
            ).collect(Collectors.toList());
        } else {
            sharePostOutputMap = new HashMap<>();
        }

        return postEntityPage.map(
                postEntity -> {
                    PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                    postOutput.setFullName(userEntityMap.get(postEntity.getUserId()).getFullName());
                    postOutput.setImageUrl(userEntityMap.get(postEntity.getUserId()).getImageUrl());
                    postOutput.setImageUrls(StringUtils.getListFromString(postEntity.getImageUrlsString()));
                    if (Objects.nonNull(postOutput.getShareId())) {
                        PostOutput sharePostOutput = sharePostOutputMap.get(postOutput.getShareId());
                        if (sharePostOutput.getState().equals(Common.PRIVATE)) {
                            sharePostOutput = null;
                        }
                        postOutput.setSharePost(sharePostOutput);
                    }
                    return postOutput;
                }
        );
    }

    private Page<PostOutput> setHasLikeForPosts(Long userId, Page<PostOutput> postOutputs) {
        List<LikeMapEntity> likeMapEntities = likeMapRepository.findAllByUserIdAndPostIdIn(
                userId,
                postOutputs.map(PostOutput::getId).toList()
        );
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()) {
            return postOutputs;
        }
        Map<Long, Long> likeMapsMap = likeMapEntities.stream()
                .collect(Collectors.toMap(LikeMapEntity::getPostId, LikeMapEntity::getId));
        return postOutputs.map(
                postOutput -> {
                    postOutput.setHasLike(likeMapsMap.containsKey(postOutput.getId()));
                    return postOutput;
                }
        );
    }
}
