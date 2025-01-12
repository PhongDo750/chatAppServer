package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.post.PostInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.entity.LikeMapEntity;
import com.example.chatAppServer.entity.NotificationEntity;
import com.example.chatAppServer.entity.PostEntity;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.entity.friend.FriendMapEntity;
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

@Service
@AllArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CustomRepository customRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final LikeMapRepository likeMapRepository;
    private final FriendMapRepository friendMapRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createPost(String accessToken, PostInput postInput, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = PostEntity.builder()
                .userId(userId)
                .content(postInput.getContent())
                .state(postInput.getState())
                .type(Common.USER)
                .imageUrlsString(
                        Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
                )
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.save(postEntity);
    }

    @Transactional
    public void updatePost(String accessToken, Long postId, PostInput updatePostInput, List<MultipartFile> imageUrls) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        PostEntity postEntity = customRepository.getPostBy(postId);
        if (userId.equals(postEntity.getUserId())) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        postEntity.setContent(updatePostInput.getContent());
        postEntity.setState(updatePostInput.getState());
        postEntity.setImageUrlsString(
                Objects.nonNull(imageUrls) ? StringUtils.convertListToString(StringUtils.getImageUrls(imageUrls)) : null
        );
        postRepository.save(postEntity);
    }

    @Transactional
    public void deletePost(String accessToken, Long postId) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        postRepository.deleteByUserIdAndId(userId, postId);
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
                            .type(Common.USER)
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
    public Page<PostOutput> getMyPosts(String accessToken, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Page<PostEntity> postEntities = postRepository.findAllByUserId(userId, pageable);
        if (Objects.isNull(postEntities) || postEntities.isEmpty()) {
            return Page.empty();
        }

        UserEntity userEntity = customRepository.getUserBy(userId);
        Map<Long, UserEntity> userEntityMap = new HashMap<>();
        userEntityMap.put(userId, userEntity);
        return setHasLikeForPosts(userId, mapResponsePostPage(postEntities, userEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostsOfFriends(String accessToken, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        Set<Long> friendIds = new HashSet<>();
        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }
        friendIds = friendIds.stream().filter(id -> !id.equals(userId)).collect(Collectors.toSet());

        Page<PostEntity> postEntitiesOfFriends = postRepository.findAllByUserIdInAndState(friendIds, Common.PUBLIC, pageable);
        if (Objects.isNull(postEntitiesOfFriends) || postEntitiesOfFriends.isEmpty()) {
            return Page.empty();
        }

        Map<Long, UserEntity> friendMapEntityMap = userRepository.findAllByIdIn(
                        postEntitiesOfFriends.stream().map(PostEntity::getUserId).distinct().collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return setHasLikeForPosts(userId, mapResponsePostPage(postEntitiesOfFriends, friendMapEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostsByUserId(Long userId, String accessToken, Pageable pageable){
        Page<PostEntity> postEntityPage = postRepository.findAllByUserIdAndState(userId, Common.PUBLIC, pageable);
        if (Objects.isNull(postEntityPage) || postEntityPage.isEmpty()) {
            return Page.empty();
        }
        UserEntity userEntity = customRepository.getUserBy(userId);
        Map<Long, UserEntity> userEntityMap = new HashMap<>();
        userEntityMap.put(userEntity.getId(), userEntity);
        return setHasLikeForPosts(TokenHelper.getUserIdFromToken(accessToken), mapResponsePostPage(postEntityPage, userEntityMap));
    }

    @Transactional(readOnly = true)
    public Page<PostOutput> getPostOfListFriend(String accessToken, Long friendId, Pageable pageable){
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        FriendMapEntity friendMapEntity = friendMapRepository.findByUserId1AndUserId2(userId,friendId);
        if (Objects.isNull(friendMapEntity)) {
            throw  new RuntimeException(Common.ACTION_FAIL);
        }
        Page<PostEntity> PostsOfFriendProfile = postRepository.findAllByUserIdAndState(friendId,Common.PUBLIC,pageable);
        Map<Long, UserEntity> friendMap = new HashMap<>();
        UserEntity friendEntity = customRepository.getUserBy(friendId);
        friendMap.put(friendEntity.getId(),friendEntity);
        return setHasLikeForPosts(userId,mapResponsePostPage(PostsOfFriendProfile,friendMap));
    }


    private Page<PostOutput> mapResponsePostPage(Page<PostEntity> postEntityPage, Map<Long, UserEntity> userEntityMap) {
        List<Long> shareIds = new ArrayList<>();
        for (PostEntity postEntity : postEntityPage) {
            if (Objects.nonNull(postEntity.getShareId())) {
                shareIds.add(postEntity.getShareId());
            }
        }

        Map<Long, PostOutput> sharePostOutputMap = new HashMap<>();
        if (!shareIds.isEmpty()) {
            List<PostEntity> sharePostEntities = postRepository.findAllByIdIn(shareIds);

            Map<Long, UserEntity> userOfSharePostEntities = userRepository.findAllByIdIn(
                    sharePostEntities.stream().map(PostEntity::getUserId).collect(Collectors.toSet())
            ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

            for (PostEntity sharePostEntity : sharePostEntities) {
                UserEntity userEntity = userOfSharePostEntities.get(sharePostEntity.getUserId());
                PostOutput postOutput = postMapper.getOutputFromEntity(sharePostEntity);
                postOutput.setUserId(userEntity.getId());
                postOutput.setState(sharePostEntity.getState());
                postOutput.setFullName(userEntity.getFullName());
                postOutput.setImageUrl(userEntity.getImageUrl());
                postOutput.setImageUrls(
                        sharePostEntity.getImageUrlsString() != null ?
                                StringUtils.getListFromString(sharePostEntity.getImageUrlsString()) : null
                );
                sharePostOutputMap.put(sharePostEntity.getId(), postOutput);
            }
        }

        return postEntityPage.map(
                postEntity -> {
                    PostOutput postOutput = postMapper.getOutputFromEntity(postEntity);
                    postOutput.setFullName(userEntityMap.get(postEntity.getUserId()).getFullName());
                    postOutput.setImageUrl(userEntityMap.get(postEntity.getUserId()).getImageUrl());
                    postOutput.setImageUrls(
                            postEntity.getImageUrlsString() != null ?
                                    StringUtils.getListFromString(postEntity.getImageUrlsString()) : null
                    );
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
        if (Objects.isNull(likeMapEntities) || likeMapEntities.isEmpty()){
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
