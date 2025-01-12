package com.example.chatAppServer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tbl_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String imageUrl;
    private String backgroundUrl;
    private OffsetDateTime birthday;
    private String gender;
    private String description;
    private String googleId;
    private String email;
}