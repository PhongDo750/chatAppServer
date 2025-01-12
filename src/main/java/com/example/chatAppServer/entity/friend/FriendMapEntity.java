package com.example.chatAppServer.entity.friend;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "tbl_friend_map")
@Builder
public class FriendMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id_1")
    private Long userId1;
    @Column(name = "user_id_2")
    private Long userId2;
}
