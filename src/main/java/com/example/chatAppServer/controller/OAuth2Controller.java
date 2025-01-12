package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.user.ResponseToken;
import com.example.chatAppServer.service.OAuth2Service;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;

    @GetMapping("/google-url")
    public ResponseEntity<String> generateAuthUrl(){
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(oAuth2Service.generateAuthUrl());
    }

    @PostMapping("/logIn")
    public ResponseToken getAccessToken(@RequestParam String code) throws ParseException {
        return oAuth2Service.logIn(code);
    }
}
