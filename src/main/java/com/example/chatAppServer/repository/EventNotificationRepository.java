package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.message.EventNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventNotificationRepository extends JpaRepository<EventNotificationEntity, Long> {
    void deleteAllByUserIdAndEventType(Long userId, String eventType);

    List<EventNotificationEntity> findAllByUserIdAndState(Long userId, String state);
}
