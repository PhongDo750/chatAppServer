package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.UserGroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Long> {
    boolean existsByGroupIdAndUserIdIn(Long groupId, List<Long> userIds);

    UserGroupEntity findByGroupIdAndUserId(Long groupId, Long userId);

    void deleteAllByGroupIdAndUserIdIn(Long groupId, List<Long> userIds);

    void deleteAllByGroupId(Long groupId);

    void deleteByUserIdAndGroupId(Long userId, Long groupId);

    Page<UserGroupEntity> findAllByGroupId(Long groupId, Pageable pageable);

    UserGroupEntity findByRoleAndGroupId(String role, Long groupId);

    List<UserGroupEntity> findAllByGroupId(Long groupId);
}
