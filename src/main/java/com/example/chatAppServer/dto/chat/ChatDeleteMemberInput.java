package com.example.chatAppServer.dto.chat;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatDeleteMemberInput {
    private Long groupChatId;
    private Long userId;
}
