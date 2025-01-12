package com.example.chatAppServer.entity.message;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_event_notification")
@Builder
public class EventNotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String fullName;
    private String imageUrl;
    private String eventType;
    private String state;
    private Long chatId;
    private Long message;
    private LocalDateTime createdAt;
}
