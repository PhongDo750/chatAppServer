package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    boolean existsByName(String name);
}
