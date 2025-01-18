package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.chat.*;
import com.example.chatAppServer.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/group-chat")
@AllArgsConstructor
@CrossOrigin
public class GroupChatController {
    private final GroupChatService groupchatService;

    @Operation(summary = "Tạo nhóm chat")
    @PostMapping
    public void createGroupChat(@RequestBody @Valid CreateGroupChatInput chatInput,
                                @RequestHeader(value = "Authorization") String accessToken) {
        groupchatService.createGroupChat(accessToken, chatInput);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thành viên nhóm chat")
    public List<ChatMemberOutput> getGroupMemBersBy(@RequestParam Long groupId,
                                                    @RequestHeader(value = "Authorization") String accessToken) {
        return groupchatService.getGroupChatMembers(accessToken, groupId);
    }

    @PostMapping("/add-new")
    @Operation(summary = "Thêm thành viên vào nhóm chat")
    public void addNewMemberToGroupChat(@RequestBody ChatAddNewMemberInput chatAddNewMemberInput,
                                        @RequestHeader(value = "Authorization") String accessToken) {
        groupchatService.addNewMemberToGroupChat(chatAddNewMemberInput, accessToken);
    }

    @DeleteMapping("/delete-member")
    @Operation(summary = "Xóa thành viên khỏi nhóm chat")
    public void deleteMember(@RequestHeader("Authorization") String accessToken,
                             @RequestBody ChatDeleteMemberInput chatDeleteMemberInput) {
        groupchatService.deleteMember(accessToken, chatDeleteMemberInput);
    }
    @Operation(summary = "Tìm kiếm nhóm chat")
    @GetMapping("/search")
    public Page<GroupChatOutPut> getGroups(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return groupchatService.getGroups(accessToken,search, pageable);
    }

    @DeleteMapping("/leave-group")
    @Operation(summary = "Rời nhóm chat")
    public void leaveTheGroupChat(@RequestHeader("Authorization") String accessToken,
                                  @RequestParam Long chatId) {
        groupchatService.leaveTheGroupChat(accessToken, chatId);
    }
}
