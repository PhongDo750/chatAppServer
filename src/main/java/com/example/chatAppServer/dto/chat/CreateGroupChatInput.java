package com.example.chatAppServer.dto.chat;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupChatInput {
    @NotEmpty
    private String name;
    @NotEmpty
    private List<Long> userIds;
}