package com.example.chatAppServer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_post")
@Builder
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String content;
    @Column(name = "image_urls_string")
    private String imageUrlsString;
    private String type;
    private String state;
    private LocalDateTime createdAt;
    private Long shareId;
    private Long groupId;
}
