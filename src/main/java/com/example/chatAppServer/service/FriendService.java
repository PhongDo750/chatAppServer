package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.user.FriendSearchingOutput;
import com.example.chatAppServer.dto.user.UserOutput;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.entity.friend.FriendMapEntity;
import com.example.chatAppServer.entity.friend.FriendRequestEntity;
import com.example.chatAppServer.mapper.UserMapper;
import com.example.chatAppServer.repository.FriendMapRepository;
import com.example.chatAppServer.repository.FriendRequestRepository;
import com.example.chatAppServer.repository.UserRepository;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FriendService {
    private final FriendMapRepository friendMapRepository;
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserMapper userMapper;

    @Transactional
    public void sendRequestAddFriend(String accessToken, Long receiverId) {
        Long senderId = TokenHelper.getUserIdFromToken(accessToken);
        if (receiverId.equals(senderId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(receiverId, senderId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(friendMapRepository.existsByUserId1AndUserId2(senderId, receiverId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(friendMapRepository.existsByUserId1AndUserId2(receiverId, senderId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        FriendRequestEntity friendRequestEntity = FriendRequestEntity.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .createdAt(LocalDateTime.now())
                .build();
        friendRequestRepository.save(friendRequestEntity);
    }

    @Transactional
    public void deleteSendFriendRequest(String accessToken, Long receiverId){
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        if(Boolean.FALSE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(userId,receiverId))){
            throw new RuntimeException(Common.RECORD_NOT_FOUND);
        }
        friendRequestRepository.deleteByReceiverIdAndSenderId(receiverId, userId);
    }

    @Transactional
    public void acceptAddFriendRequest(Long senderId, String accessToken) {
        Long receiverId = TokenHelper.getUserIdFromToken(accessToken);
        if (Boolean.FALSE.equals(friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        friendMapRepository.save(FriendMapEntity.builder()
                .userId1(receiverId)
                .userId2(senderId)
                .build()
        );

        friendRequestRepository.deleteByReceiverIdAndSenderId(receiverId, senderId);
    }

    @Transactional
    public void rejectAddFriendRequest(Long senderId, String accessToken) {
        Long receiverId = TokenHelper.getUserIdFromToken(accessToken);
        friendRequestRepository.deleteByReceiverIdAndSenderId(receiverId, senderId);
    }

    @Transactional
    public void deleteFriend(Long friendId, String accessToken) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        friendMapRepository.deleteByUserId1AndUserId2(userId, friendId);
        friendMapRepository.deleteByUserId1AndUserId2(friendId, userId);
    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getFriends(String accessToken , Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Page<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId, pageable);
        if (Objects.isNull(friendMapEntities) || friendMapEntities.isEmpty()) {
            return Page.empty();
        }

        Set<Long> friendIds = new HashSet<>();
        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }

        friendIds.remove(userId);
        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(friendIds)
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return friendMapEntities.map(
                friendMapEntity -> {
                    UserEntity userEntity = null;
                    if (userEntityMap.containsKey(friendMapEntity.getUserId1())) {
                        userEntity = userEntityMap.get(friendMapEntity.getUserId1());
                    } else {
                        userEntity = userEntityMap.get(friendMapEntity.getUserId2());
                    }
                    return UserOutput.builder()
                            .id(userEntity.getId())
                            .fullName(userEntity.getFullName())
                            .imageUrl(userEntity.getImageUrl())
                            .build();
                }
        );

    }

    @Transactional(readOnly = true)
    public Page<UserOutput> getFriendsBySearch(String accessToken, String search, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Page<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId, pageable);
        List<Long> friendIds = new ArrayList<>();
        if (Objects.isNull(friendMapEntities) || friendMapEntities.isEmpty()) {
            return Page.empty();
        }

        for (FriendMapEntity friendMapEntity : friendMapEntities) {
            friendIds.add(friendMapEntity.getUserId1());
            friendIds.add(friendMapEntity.getUserId2());
        }
        friendIds = friendIds.stream()
                .filter(friendId -> !friendId.equals(userId))
                .distinct()
                .collect(Collectors.toList());
        if(Objects.isNull(search)){
            return userRepository.findAllByIdIn(friendIds, pageable).map(
                    userEntity ->{
                        return UserOutput.builder()
                                .id(userEntity.getId())
                                .imageUrl(userEntity.getImageUrl())
                                .fullName(userEntity.getFullName())
                                .build();
                    });
        }
        Page<UserEntity> userEntities = userRepository.findAllByIdInAndSearch(friendIds, search, pageable);
        return userEntities.map(
                userEntity ->{
                    return UserOutput.builder()
                            .id(userEntity.getId())
                            .imageUrl(userEntity.getImageUrl())
                            .fullName(userEntity.getFullName())
                            .build();
                });
    }

    @Transactional(readOnly = true)
    public Page<FriendSearchingOutput> findUsers(String accessToken, String search, Pageable pageable) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        Page<UserEntity> userEntities = userRepository.findAllByNotIdInAndSearch(Arrays.asList(userId), search, pageable);

        if (userEntities.isEmpty()) {
            return Page.empty();
        }
        Map<Long, Long> friendMap = new HashMap<>();
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        if (Objects.nonNull(friendMapEntities) && !friendMapEntities.isEmpty()) {
            friendMap = friendMapEntities.stream()
                    .distinct()
                    .collect(Collectors.toMap(FriendMapEntity::getId, FriendMapEntity::getId));
        }

        Map<Long, Long> friendRequestMap = new HashMap<>();
        List<FriendRequestEntity> friendRequestEntities = friendRequestRepository.findAllBySenderId(userId);
        if (Objects.nonNull(friendRequestEntities) && !friendMapEntities.isEmpty()) {
            friendRequestMap = friendRequestEntities.stream().collect(Collectors.toMap(FriendRequestEntity::getReceiverId, FriendRequestEntity::getSenderId));
        }

        Map<Long, Long> finalFriendMap = friendMap;
        Map<Long, Long> finalFriendRequestMap = friendRequestMap;
        Page<FriendSearchingOutput> friendSearchingOutputs = userEntities.map(
                userEntity -> {
                    FriendSearchingOutput friendSearching = userMapper.getFriendSearchingFrom(userEntity);
                    friendSearching.setIsFriend(finalFriendMap.containsKey(friendSearching.getId()));
                    friendSearching.setHadSendFriendRequest(finalFriendRequestMap.containsKey(friendSearching.getId()));
                    return friendSearching;
                }
        );

        return setIsFriendOrHasRequestFriendForUsers(userId, friendSearchingOutputs);
    }

    private Page<FriendSearchingOutput> setIsFriendOrHasRequestFriendForUsers(Long userId,
                                                                              Page<FriendSearchingOutput> users){
        if (Objects.isNull(users)){
            return Page.empty();
        }
        List<Long> userIds = users.stream().map(FriendSearchingOutput::getId).collect(Collectors.toList());
        // lấy map những thằng mình đã send friend request nhưng nó chưa đồng ý
        Map<Long, Long> userSendRequestMap = friendRequestRepository.findAllBySenderIdAndReceiverIdIn(userId, userIds).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getReceiverId, FriendRequestEntity::getSenderId));
        // Lấy map những thằng đã send friend request nhưng mình chưa đồng ý
        Map<Long, Long> userReceiverRequestMap = friendRequestRepository.findAllBySenderIdInAndReceiverId(userIds, userId).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getSenderId, FriendRequestEntity::getReceiverId));
        // Lấy những thằng đã là bạn bè mình rồi
        List<FriendMapEntity> friendMapEntities = friendMapRepository.findAllByUserId(userId);
        Map<Long, Long> friendMap = friendMapEntities.stream().map(
                friendMapEntity -> {
                    if (userId.equals(friendMapEntity.getUserId1())){
                        return friendMapEntity.getUserId2();
                    }
                    return friendMapEntity.getUserId1();
                }
        ).collect(Collectors.toMap(Function.identity(), Function.identity()));

        return users.map(friendSearchingOutput -> {
            if (userSendRequestMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setHadSendFriendRequest(true);
            }
            else {
                friendSearchingOutput.setHadSendFriendRequest(false);
            }

            if (userReceiverRequestMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setHadReceiverFriendRequest(true);
            }
            else {
                friendSearchingOutput.setHadReceiverFriendRequest(false);
            }

            if (friendMap.containsKey(friendSearchingOutput.getId())){
                friendSearchingOutput.setIsFriend(true);
            }
            else {
                friendSearchingOutput.setIsFriend(false);
            }
            return friendSearchingOutput;
        });
    }
}
