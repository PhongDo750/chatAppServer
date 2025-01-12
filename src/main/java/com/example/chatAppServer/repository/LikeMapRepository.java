package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.LikeMapEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeMapRepository extends JpaRepository<LikeMapEntity, Long> {
    List<LikeMapEntity> findAllByUserIdAndPostIdIn(Long userId, List<Long> postIds);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    Page<LikeMapEntity> findAllByPostId(Long postId, Pageable pageable);

    int countAllByPostId(Long postId);
}
