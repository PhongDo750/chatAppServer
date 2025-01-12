package com.example.chatAppServer.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendSearchingOutput {
    private Long id;
    private String imageUrl;
    private String fullName;
    private Boolean isFriend;
    private Boolean hadSendFriendRequest;
    private Boolean hadReceiverFriendRequest;
}