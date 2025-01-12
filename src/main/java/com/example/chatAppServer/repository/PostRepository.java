package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    void deleteByUserIdAndId(Long userId, Long postId);

    Page<PostEntity> findAllByUserId(Long userId, Pageable pageable);

    List<PostEntity> findAllByIdIn(List<Long> postIds);

    Page<PostEntity> findAllByUserIdInAndState(Set<Long> userIds, String state, Pageable pageable);

    Page<PostEntity> findAllByUserIdAndState(Long userId, String state, Pageable pageable);

    Page<PostEntity> findAllByGroupIdAndType(Long groupId, String type, Pageable pageable);

    int countAllByShareId(Long shareId);
}
