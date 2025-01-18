package com.example.chatAppServer.dto.chat;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GroupChatOutPut {
    private Long id;
    private String name;
    private String img;
}
