package com.example.chatAppServer.controller;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import nl.martijndwars.webpush.Subscription;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/push-notification")
public class PushNotificationController {
    private final PushNotificationService pushNotificationService;

    @Operation(summary = "Lấy public key của server")
    public String getPublicServerKey() {
        return pushNotificationService.getPublicKey();
    }

    @Operation(summary = "Đăng ký nhận thông báo từ server")
    @PostMapping("/subscribe")
    public void subscribe(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                          @RequestBody Subscription subscription) {
        pushNotificationService.subscribe(accessToken, subscription);
    }

    @Operation(summary = "Hủy đăng ký nhận thông báo")
    @DeleteMapping("/unsubscribe")
    public void unsubscribe(@RequestHeader(Common.AUTHORIZATION) String accessToken,
                            @RequestParam String endpoint) {
        pushNotificationService.unsubscribe(accessToken, endpoint);
    }
}
