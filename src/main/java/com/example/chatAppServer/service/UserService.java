package com.example.chatAppServer.service;

import com.example.chatAppServer.cloudinary.CloudinaryHelper;
import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.user.ChangeInfoUserRequest;
import com.example.chatAppServer.dto.user.UserOutputV2;
import com.example.chatAppServer.dto.user.UserRequest;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.mapper.UserMapper;
import com.example.chatAppServer.repository.CustomRepository;
import com.example.chatAppServer.repository.UserRepository;
import com.example.chatAppServer.token.TokenHelper;
import lombok.AllArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomRepository customRepository;

    @Transactional
    public String signUp(UserRequest signUpRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
            throw new RuntimeException(Common.USERNAME_IS_EXISTS);
        }

        signUpRequest.setPassword(BCrypt.hashpw(signUpRequest.getPassword(), BCrypt.gensalt()));
        UserEntity userEntity = userMapper.getEntityFromRequest(signUpRequest);
        userEntity.setImageUrl(Common.IMAGE_DEFAULT);
        userEntity.setBackgroundUrl(Common.IMAGE_DEFAULT);
        userRepository.save(userEntity);
        return TokenHelper.generateToken(userEntity);
    }

    @Transactional
    public String logIn(UserRequest loginRequest) {
        UserEntity userEntity = userRepository.findByUsername(loginRequest.getUsername());
        if (Objects.isNull(userEntity)) {
            throw new RuntimeException(Common.ACTION_FAIL);
        }

        if (!BCrypt.checkpw(loginRequest.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException(Common.INCORRECT_PASSWORD);
        }
        return TokenHelper.generateToken(userEntity);
    }

    @Transactional
    public void changeUserInformation(String accessToken,
                               ChangeInfoUserRequest changeInfoUserRequest,
                               MultipartFile imageUrl, MultipartFile backgroundUrl) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = customRepository.getUserBy(userId);
        userMapper.updateEntityFromInput(userEntity, changeInfoUserRequest);
        OffsetDateTime birthday = OffsetDateTime.parse(changeInfoUserRequest.getBirthdayString());
        userEntity.setBirthday(birthday);
        if (Objects.nonNull(imageUrl)) {
            userEntity.setImageUrl(CloudinaryHelper.uploadAndGetFileUrl(imageUrl));
        }

        if (Objects.nonNull(backgroundUrl)) {
            userEntity.setBackgroundUrl(CloudinaryHelper.uploadAndGetFileUrl(backgroundUrl));
        }
        userRepository.save(userEntity);
    }

    @Transactional(readOnly = true)
    public UserOutputV2 getUserInformation(String accessToken){
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = customRepository.getUserBy(userId);
        return userMapper.getOutputFromEntity(userEntity);
    }
}
