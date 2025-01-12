package com.example.chatAppServer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_notification")
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; //group or user
    private Long userId;
    private Long interactId;
    private Long groupId;
    private Long postId;
    private LocalDateTime createdAt;
    private Boolean hasSeen;
    private String interactType;
}
