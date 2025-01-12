package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.friend.FriendMapEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendMapRepository extends JpaRepository<FriendMapEntity, Long> {
    boolean existsByUserId1AndUserId2(Long userId1, Long userId2);

    void deleteByUserId1AndUserId2(Long userId1, Long userId2);

    @Query("SELECT f from FriendMapEntity f where f.userId1 =:userId or f.userId2 =:userId")
    Page<FriendMapEntity> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT f from FriendMapEntity f where f.userId1 =:userId or f.userId2 =:userId")
    List<FriendMapEntity> findAllByUserId(Long userId);

    FriendMapEntity findByUserId1AndUserId2(Long userId1, Long userId2);
}

