package com.example.chatAppServer.dto.group;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GroupAddNewMemberInput {
    private Long groupId;
    @Size(min = 1)
    private List<Long> userIds;
}
