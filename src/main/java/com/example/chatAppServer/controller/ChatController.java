package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.chat.ChatOutput;
import com.example.chatAppServer.dto.message.MessageOutput;
import com.example.chatAppServer.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Xem list chat")
    @GetMapping("/get-list-chat")
    public Page<ChatOutput> getChatList(@RequestHeader("Authorization") String accessToken,
                                        @ParameterObject Pageable pageable) {
        return chatService.getChatList(accessToken, pageable);
    }

    @Operation(summary = "Xem message")
    @GetMapping("/get-messages")
    public Page<MessageOutput> getMessages(@RequestHeader("Authorization") String accessToken,
                                           @RequestParam Long chatId,
                                           @ParameterObject Pageable pageable) {
        return chatService.getMessages(accessToken, chatId, pageable);
    }
}
