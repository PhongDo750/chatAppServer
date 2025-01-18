package com.example.chatAppServer.dto.post;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostInput {
    private String content;
    @Pattern(regexp = "^(PRIVATE|PUBLIC)")
    private String state;
    private String type;
    private Long groupId;
}
