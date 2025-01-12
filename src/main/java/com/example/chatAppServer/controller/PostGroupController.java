package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.post.PostGroupInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.service.PostGroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/post-group")
@AllArgsConstructor
@CrossOrigin
public class PostGroupController {
    private final PostGroupService postGroupService;

    @Operation(summary = "Danh sách bài viết trong group")
    @GetMapping("/get-post")
    public Page<PostOutput> getPostsOfFriends(@RequestHeader("Authorization") String accessToken,
                                              @RequestParam Long groupId,
                                              @ParameterObject Pageable pageable){
        return postGroupService.getPostGroup(accessToken,groupId, pageable);
    }

    @Operation(summary = "Đăng bài viết")
    @PostMapping(value = "/post", consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public void creatPost(@RequestHeader("Authorization") String accessToken,
                          @RequestPart("post_information") @Valid String createPostInformation,
                          @RequestPart(name = "images",required = false) List<MultipartFile> multipartFiles) throws JsonProcessingException {
        PostGroupInput postGroupInput ;
        ObjectMapper objectMapper = new ObjectMapper();
        postGroupInput = objectMapper.readValue(createPostInformation, PostGroupInput.class);
        postGroupService.createPost(accessToken, postGroupInput,multipartFiles);
    }

    @Operation(summary = "Sửa bài viết")
    @PutMapping("/update")
    public void updatePost(@RequestHeader("Authorization") String accessToken,
                           @RequestPart("post_information") @Valid String updatePostInformation,
                           @RequestParam Long postId,
                           @RequestPart(name = "images") List<MultipartFile> multipartFiles) throws JsonProcessingException {
        PostGroupInput updatePostInput;
        ObjectMapper objectMapper = new ObjectMapper();
        updatePostInput = objectMapper.readValue(updatePostInformation,PostGroupInput.class);
        postGroupService.updatePost(accessToken, postId, updatePostInput, multipartFiles);
    }

    @Operation(summary = "Xóa bài viết")
    @DeleteMapping("/delete")
    public void deletePost(@RequestHeader("Authorization") String accessToken,
                           @RequestParam Long postId,
                           @RequestParam Long groupId){
        postGroupService.deletePost(accessToken, postId,groupId);
    }
}