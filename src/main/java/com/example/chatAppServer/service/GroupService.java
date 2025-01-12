package com.example.chatAppServer.service;

import com.example.chatAppServer.cloudinary.CloudinaryHelper;
import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.group.*;
import com.example.chatAppServer.entity.GroupEntity;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.entity.UserGroupEntity;
import com.example.chatAppServer.mapper.GroupMapper;
import com.example.chatAppServer.repository.CustomRepository;
import com.example.chatAppServer.repository.GroupRepository;
import com.example.chatAppServer.repository.UserGroupRepository;
import com.example.chatAppServer.repository.UserRepository;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final CustomRepository customRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public void createGroup(String accessToken, GroupInput groupInput, MultipartFile imageUrl) {
        Long managerId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = customRepository.getUserBy(managerId);

        if (Boolean.TRUE.equals(groupRepository.existsByName(groupInput.getName()))) {
            throw new RuntimeException(Common.USERNAME_IS_EXISTS);
        }

        if (Objects.isNull(userEntity)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        GroupEntity groupEntity = GroupEntity.builder()
                .name(groupInput.getName())
                .memberCount(groupInput.getUserIds().size() + 1)
                .build();

        if (Objects.nonNull(imageUrl)) {
            groupEntity.setImageUrl(CloudinaryHelper.uploadAndGetFileUrl(imageUrl));
        } else {
            groupEntity.setImageUrl(Common.IMAGE_DEFAULT);
        }
        groupRepository.save(groupEntity);

        UserGroupEntity userGroupEntity = UserGroupEntity.builder()
                .userId(managerId)
                .groupId(groupEntity.getId())
                .role(Common.ADMIN)
                .build();
        userGroupRepository.save(userGroupEntity);

        for (Long userId : groupInput.getUserIds()) {
            userGroupRepository.save(
                    UserGroupEntity.builder()
                            .userId(userId)
                            .groupId(groupEntity.getId())
                            .role(Common.MEMBER)
                            .build()
            );
        }
    }

    @Transactional
    public void deleteGroup(String accessToken, Long groupId) {
        Long managerId = TokenHelper.getUserIdFromToken(accessToken);
        UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(groupId, managerId);
        if (Objects.isNull(userGroupEntity) || userGroupEntity.getRole().equals(Common.MEMBER)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        groupRepository.deleteById(groupId);
        userGroupRepository.deleteAllByGroupId(groupId);
    }

    @Transactional
    public void changeInformationGroup(String accessToken,
                                       ChangeInformationGroupInput changeInformationGroupInput,
                                       MultipartFile imageUrl) {
        Long managerId = TokenHelper.getUserIdFromToken(accessToken);
        UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeInformationGroupInput.getGroupId(), managerId);
        if (Objects.isNull(userGroupEntity) || userGroupEntity.getRole().equals(Common.MEMBER)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(groupRepository.existsByName(changeInformationGroupInput.getName()))) {
            throw new RuntimeException(Common.USERNAME_IS_EXISTS);
        }

        GroupEntity groupEntity = customRepository.getGroupBy(changeInformationGroupInput.getGroupId());
        groupEntity.setName(changeInformationGroupInput.getName());
        if (Objects.nonNull(imageUrl)) {
            groupEntity.setImageUrl(CloudinaryHelper.uploadAndGetFileUrl(imageUrl));
        }
        groupRepository.save(groupEntity);
    }

    @Transactional
    public void addMemberToGroup(String accessToken, GroupAddNewMemberInput groupAddNewMemberInput) {
        if (Boolean.FALSE.equals(userGroupRepository.existsByGroupIdAndUserIdIn(
                groupAddNewMemberInput.getGroupId(), Arrays.asList(TokenHelper.getUserIdFromToken(accessToken))
        ))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (Boolean.TRUE.equals(userGroupRepository.existsByGroupIdAndUserIdIn(
                groupAddNewMemberInput.getGroupId(), groupAddNewMemberInput.getUserIds()
        ))) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        for (Long newUserId : groupAddNewMemberInput.getUserIds()) {
            userGroupRepository.save(
                    UserGroupEntity.builder()
                            .groupId(groupAddNewMemberInput.getGroupId())
                            .userId(newUserId)
                            .role(Common.MEMBER)
                            .build()
            );
        }

        GroupEntity groupEntity = customRepository.getGroupBy(groupAddNewMemberInput.getGroupId());
        groupEntity.setMemberCount(groupEntity.getMemberCount() + groupAddNewMemberInput.getUserIds().size());
        groupRepository.save(groupEntity);
    }

    @Transactional
    public void deleteMember(String accessToken, GroupDeleteMemberInput groupDeleteMemberInput) {
        Long managerId = TokenHelper.getUserIdFromToken(accessToken);
        UserGroupEntity userGroupEntity = userGroupRepository
                .findByGroupIdAndUserId(groupDeleteMemberInput.getGroupId(), managerId);

        GroupEntity groupEntity = customRepository.getGroupBy(groupDeleteMemberInput.getGroupId());
        if (groupEntity.getMemberCount() <= groupDeleteMemberInput.getUserIds().size()) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
        groupEntity.setMemberCount(groupEntity.getMemberCount() - groupDeleteMemberInput.getUserIds().size());
        groupRepository.save(groupEntity);

        if (Objects.isNull(userGroupEntity) || userGroupEntity.getRole().equals(Common.MEMBER)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (groupDeleteMemberInput.getUserIds().contains(managerId)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        userGroupRepository
                .deleteAllByGroupIdAndUserIdIn(groupDeleteMemberInput.getGroupId(), groupDeleteMemberInput.getUserIds());
    }

    @Transactional
    public void leaveGroup(String accessToken, Long groupId) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        GroupEntity groupEntity = customRepository.getGroupBy(groupId);
        if (groupEntity.getMemberCount() > 1) {
            userGroupRepository.deleteByUserIdAndGroupId(userId, groupId);
        } else {
            userGroupRepository.deleteByUserIdAndGroupId(userId,groupId);
            groupRepository.deleteById(groupId);
        }
    }

    @Transactional(readOnly = true)
    public Page<GroupMemberOutput> getGroupMembers(Long groupId, String accessToken, Pageable pageable) {
        Page<UserGroupEntity> userGroupEntities = userGroupRepository.findAllByGroupId(groupId, pageable);
        if (Objects.isNull(userGroupEntities) || userGroupEntities.isEmpty()) {
            return Page.empty();
        }

        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserGroupEntity managerEntity = userGroupRepository.findByRoleAndGroupId(Common.ADMIN, groupId);

        Map<Long, UserEntity> userEntityMap = userRepository.findAllByIdIn(
                userGroupEntities.stream().map(UserGroupEntity::getUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return userGroupEntities.map(
                userGroupEntity -> {
                    UserEntity userEntity = userEntityMap.get(userGroupEntity.getUserId());
                    return GroupMemberOutput.builder()
                            .id(userEntity.getId())
                            .fullName(userEntity.getFullName())
                            .imageUrl(userEntity.getImageUrl())
                            .role(
                                    userEntity.getId().equals(managerEntity.getUserId()) ? Common.ADMIN : Common.MEMBER
                            )
                            .build();
                }
        );
    }
}
