package com.example.chatAppServer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_chat")
@Builder
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long managerId;
    private String chatType;
    private LocalDateTime newestChatTime;
    @Column(name = "user_id1")
    private Long userId1;
    @Column(name = "user_id2")
    private Long userId2;
    private Long newestUserId;
    private String newestMessage;
    private String imageUrl;
}
