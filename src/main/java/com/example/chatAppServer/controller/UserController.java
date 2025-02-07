package com.example.chatAppServer.controller;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.user.*;
import com.example.chatAppServer.service.FriendService;
import com.example.chatAppServer.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final FriendService friendService;

    @Operation(summary = "Lấy thông tin cá nhân")
    @GetMapping
    public UserOutputV2 getUserInformation(@RequestHeader(value = Common.AUTHORIZATION) String accessToken){
        return userService.getUserInformation(accessToken);
    }

    // 2024-03-20T17:04:52.755Z
    @Operation(summary = "Thay đổi thông tin cá nhân")
    @PostMapping(value = "/change-user-information", consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public void changeUserInformation(@RequestPart("new_user_info") @Valid String changeInfoUserRequestString,
                                      @RequestHeader(value = Common.AUTHORIZATION) String accessToken,
                                      @RequestPart(value = "image", required = false) MultipartFile avatar,
                                      @RequestPart(value = "image_background", required = false) MultipartFile background) throws JsonProcessingException {
        ChangeInfoUserRequest changeInfoUserRequest;
        ObjectMapper objectMapper = new ObjectMapper();
        changeInfoUserRequest = objectMapper.readValue(changeInfoUserRequestString, ChangeInfoUserRequest.class);
        userService.changeUserInformation(accessToken,changeInfoUserRequest, avatar, background);
    }

    @Operation(summary = "Đăng ký tài khoản")
    @PostMapping("sign-up")
    public ResponseEntity signUp(@RequestBody UserRequest signUpRequest){
        return new ResponseEntity(new TokenResponse( userService.signUp(signUpRequest)), HttpStatus.OK);
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("log-in")
    public ResponseEntity logIn(@RequestBody @Valid UserRequest logInRequest){
        return new ResponseEntity(new TokenResponse(userService.logIn(logInRequest)), HttpStatus.OK);
    }

    @Operation(summary = "Tìm danh sách người dùng trên app")
    @GetMapping("/list")
    public Page<FriendSearchingOutput> findUsers(@RequestParam(required = false) String search,
                                                 @RequestHeader(value = Common.AUTHORIZATION) String accessToken,
                                                 @ParameterObject Pageable pageable){
        return friendService.findUsers(search, accessToken, pageable);
    }

    @Operation(summary = "Lấy code để reset password")
    @PostMapping("/send-code-email")
    public void sendCodeToEmail(@RequestHeader(value = Common.AUTHORIZATION) String accessToken) {
        userService.sendCodeToEmail(accessToken);
    }

    @Operation(summary = "Thay đổi mật khẩu")
    @PostMapping("/reset-password")
    public void resetPassword(@RequestHeader(value = Common.AUTHORIZATION) String accessToken,
                              @RequestBody ResetPassword resetPassword,
                              @RequestParam String code) {
        userService.resetPassword(accessToken, resetPassword, code);
    }
}
