package com.example.chatAppServer.dto.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageOutput {
    private Long id;
    private Long userId;
    private String message;
    private String fullName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Boolean isMe;
}