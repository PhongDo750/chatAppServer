package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostGroupRepository extends JpaRepository<PostEntity, Long> {
}
