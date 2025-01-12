package com.example.chatAppServer.dto.group;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class GroupMemberOutput {
    private Long id;
    private String fullName;
    private String imageUrl;
    private String role;
}
