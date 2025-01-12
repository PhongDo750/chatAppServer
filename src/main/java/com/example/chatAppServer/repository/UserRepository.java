package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    List<UserEntity> findAllByIdIn(List<Long> userIds);

    List<UserEntity> findAllByIdIn(Set<Long> userIds);

    Page<UserEntity> findAllByIdIn(List<Long> userIds, Pageable pageable);

    @Query("select u from UserEntity u where u.id in :userIds and u.fullName like %:search%")
    Page<UserEntity> findAllByIdInAndSearch(@Param("userIds") List<Long> userIds, @Param("search") String search, Pageable pageable);

    @Query("select u from UserEntity u where u.id not in :userIds and u.fullName like %:search%")
    Page<UserEntity> findAllByNotIdInAndSearch(@Param("userIds") List<Long> userIds, @Param("search") String search, Pageable pageable);

    UserEntity findByGoogleId(String googleId);
}
