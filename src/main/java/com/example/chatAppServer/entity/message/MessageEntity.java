package com.example.chatAppServer.entity.message;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_message")
@Builder
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id1")
    private Long chatId1;
    @Column(name = "chat_id2")
    private Long chatId2;
    private Long groupChatId;
    private String message;
    private LocalDateTime createAt;
    private Long senderId;
}
