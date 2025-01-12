package com.example.chatAppServer.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserOutputV2 {
    private Long id;
    private String fullName;
    private String imageUrl;
    private String backgroundUrl;
    private OffsetDateTime birthday;
    private String gender;
    private String description;
}
