package com.example.chatAppServer.dto.group;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class GroupOutput {
    private Long id;
    private String name;
    private Integer memberCount;
    private String imageUrl;
}
