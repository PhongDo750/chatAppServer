package com.example.chatAppServer.dto.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ResponseToken {
    private String accessTokenOP;
    private String accessTokenRP;
}
