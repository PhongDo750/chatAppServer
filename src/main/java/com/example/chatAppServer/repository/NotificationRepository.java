package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("SELECT n from NotificationEntity n where n.userId =:userId order by n.createdAt desc")
    Page<NotificationEntity> findAllByUserId(Long userId, Pageable pageable);
}
