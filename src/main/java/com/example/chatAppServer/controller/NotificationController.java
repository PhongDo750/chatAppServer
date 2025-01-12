package com.example.chatAppServer.controller;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.event.NotificationOutput;
import com.example.chatAppServer.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Lấy ra thông báo")
    @GetMapping
    public Page<NotificationOutput> getNotifies(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                                                @ParameterObject Pageable pageable){
        return notificationService.getNotifies(accessToken, pageable);
    }
}
