package com.example.chatAppServer.controller;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.user.FriendSearchingOutput;
import com.example.chatAppServer.dto.user.UserOutput;
import com.example.chatAppServer.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/friend")
public class FriendController {
    private final FriendService friendService;

    @Operation(summary = "Gửi yêu cầu kết bạn")
    @PostMapping("/send-request")
    public void sendRequestAddFriend(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                     @RequestParam Long receiverId) {
        friendService.sendRequestAddFriend(accessToken, receiverId);
    }

    @Operation(summary = "Xóa yêu cầu kết bạn")
    @DeleteMapping("/delete-request")
    public void deleteSendFriendRequest(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                        @RequestParam  Long receiverId) {
        friendService.deleteSendFriendRequest(accessToken, receiverId);
    }

    @Operation(summary = "Chấp nhận yêu cầu kết bạn")
    @PostMapping("/accept-request")
    public void acceptAddFriendRequest(@RequestParam Long senderId,
                                       @RequestHeader(Common.AUTHORIZATION) String accessToken) {
        friendService.acceptAddFriendRequest(senderId, accessToken);
    }

    @Operation(summary = "Từ chối yêu cầu kết bạn")
    @DeleteMapping("/reject-request")
    public void rejectAddFriendRequest(@RequestParam Long senderId,
                                       @RequestHeader(Common.AUTHORIZATION) String accessToken) {
        friendService.rejectAddFriendRequest(senderId, accessToken);
    }

    @Operation(summary = "Xóa bạn")
    @DeleteMapping("/delete-friend")
    public void deleteFriend(@RequestParam Long friendId,
                             @RequestHeader(Common.AUTHORIZATION) String accessToken) {
        friendService.deleteFriend(friendId, accessToken);
    }

    @Operation(summary = "Lấy ra danh sách bạn bè")
    @GetMapping("/get-friends")
    public Page<UserOutput> getFriends(@RequestHeader(Common.AUTHORIZATION) String accessToken ,
                                       @ParameterObject Pageable pageable) {
        return friendService.getFriends(accessToken, pageable);
    }

    @Operation(summary = "Lấy ra bạn bè theo tìm kiếm")
    @GetMapping("/get-friends-by-search")
    public Page<UserOutput> getFriendsBySearch(@RequestHeader("Authorization") String accessToken,
                                               @RequestParam(name = "search", required = false) String search,
                                               @ParameterObject Pageable pageable){
        return friendService.getFriendsBySearch(accessToken, search, pageable);
    }

    @Operation(summary = "Tìm kiếm người dùng")
    @GetMapping("/find-users")
    public Page<FriendSearchingOutput> findUsers(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                                 @RequestParam(name = "search") String search,
                                                 @ParameterObject Pageable pageable){
        return friendService.findUsers(accessToken, search, pageable);
    }
}
