package com.example.chatAppServer.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostOutput {
    private Long id;
    private Long userId;
    private String state;
    private String fullName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String content;
    private List<String> imageUrls;
    private Long shareId;
    private PostOutput sharePost;
    // shareId != null && sharePost == null

    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private List<CommentOutput> comments;
    private Boolean hasLike;
}
