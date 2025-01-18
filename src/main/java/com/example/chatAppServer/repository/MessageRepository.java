package com.example.chatAppServer.repository;

import com.example.chatAppServer.entity.message.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    @Query("select m from MessageEntity m where m.chatId1 = :chatId or m.chatId2 = :chatId or m.groupChatId =: chatId order by m.createAt desc")
    Page<MessageEntity> searchAllByChatId(Long chatId, Pageable pageable);
}
