package com.example.chatAppServer.controller;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.group.*;
import com.example.chatAppServer.service.GroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/api/v1/group")
public class GroupController {
    private final GroupService groupService;

    @Operation(summary = "Tạo group")
    @PostMapping("/create")
    public void createGroup(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                            @RequestPart("group_input") String groupInputString,
                            @RequestPart(value = "image", required = false) MultipartFile image) throws JsonProcessingException {
        GroupInput groupInput;
        ObjectMapper objectMapper = new ObjectMapper();
        groupInput = objectMapper.readValue(groupInputString, GroupInput.class);
        groupService.createGroup(accessToken, groupInput, image);
    }

    @Operation(summary = "Xóa group")
    @DeleteMapping("/delete")
    public void deleteGroup(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                            @RequestParam Long groupId) {
        groupService.deleteGroup(accessToken, groupId);
    }

    @Operation(summary = "Thay đổi thông tin group")
    @PostMapping("/update")
    public void changeInformationGroup(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                       @RequestPart("change_information") String changeInformationGroupString,
                                       @RequestPart(value = "image", required = false) MultipartFile image) throws JsonProcessingException {
        ChangeInformationGroupInput changeInformationGroupInput;
        ObjectMapper objectMapper = new ObjectMapper();
        changeInformationGroupInput = objectMapper.readValue(changeInformationGroupString, ChangeInformationGroupInput.class);
        groupService.changeInformationGroup(accessToken, changeInformationGroupInput, image);
    }

    @Operation(summary = "Thêm thành viên nhóm")
    @PostMapping("/add-new-member")
    public void addMemberToGroup(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                 @RequestBody GroupAddNewMemberInput groupAddNewMemberInput) {
        groupService.addMemberToGroup(accessToken, groupAddNewMemberInput);
    }

    @Operation(summary = "Xóa thành viên khỏi nhóm")
    @DeleteMapping("/delete-member")
    public void deleteMember(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                             @RequestBody GroupDeleteMemberInput groupDeleteMemberInput) {
        groupService.deleteMember(accessToken, groupDeleteMemberInput);
    }

    @Operation(summary = "Rời nhóm")
    @PostMapping("/leave-group")
    public void leaveGroup(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                           @RequestParam Long groupId) {
        groupService.leaveGroup(accessToken, groupId);
    }

    @Operation(summary = "Xem thành viên nhóm")
    @GetMapping("/get-group-members")
    public Page<GroupMemberOutput> getGroupMembers(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                                   @RequestParam Long groupId,
                                                   @ParameterObject Pageable pageable) {
        return groupService.getGroupMembers(groupId, accessToken, pageable);
    }
}
