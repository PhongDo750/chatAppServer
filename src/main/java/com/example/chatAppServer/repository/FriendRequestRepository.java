package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.friend.FriendRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    void deleteByReceiverIdAndSenderId(Long receiverId, Long senderId);

    List<FriendRequestEntity> findAllBySenderId(Long senderId);

    List<FriendRequestEntity> findAllBySenderIdAndReceiverIdIn(Long senderId, List<Long> receiverIds);

    List<FriendRequestEntity> findAllBySenderIdInAndReceiverId(List<Long> senderIds,Long receiverId);
}
