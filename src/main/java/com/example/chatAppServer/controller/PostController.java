package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.post.PostInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;

    @Operation(summary = "Đăng bài viết")
    @PostMapping("/post")
    public void creatPost(@RequestHeader("Authorization") String accessToken,
                          @RequestPart @Valid String createPostInputString,
                          @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles) throws JsonProcessingException {
        PostInput createPostInput ;
        ObjectMapper objectMapper = new ObjectMapper();
        createPostInput = objectMapper.readValue(createPostInputString, PostInput.class);
        postService.createPost(accessToken, createPostInput, multipartFiles);
    }

    @Operation(summary = "Sửa bài viết")
    @PutMapping("/update")
    public void updatePost(@RequestHeader("Authorization") String accessToken,
                           @RequestParam Long postId,
                           @RequestPart @Valid String updatePostInputString,
                           @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles) throws JsonProcessingException {
        PostInput updatePostInput;
        ObjectMapper objectMapper = new ObjectMapper();
        updatePostInput = objectMapper.readValue(updatePostInputString, PostInput.class);
        postService.updatePost(accessToken, postId, updatePostInput, multipartFiles);
    }

    @Operation(summary = "Xóa bài viết")
    @DeleteMapping("/delete")
    public void deletePost(@RequestHeader("Authorization") String accessToken,
                           @RequestParam Long postId){
        postService.deletePost(accessToken, postId);
    }

    @Operation(summary = "Danh sách bài viết (của mình)")
    @GetMapping("/list/me")
    public Page<PostOutput> getMyPost(@RequestHeader("Authorization") String accessToken,
                                      @ParameterObject Pageable pageable){
        return postService.getMyPosts(accessToken, pageable);
    }

    @Operation(summary = "Danh sách bài viết PUBLIC của bạn bè")
    @GetMapping("/list/friends")
    public Page<PostOutput> getPostsOfFriends(@RequestHeader("Authorization") String accessToken,
                                              @ParameterObject Pageable pageable){
        return postService.getPostsOfFriends(accessToken, pageable);
    }

    @Operation(summary = "Lấy post của bạn bè")
    @GetMapping("/list/post-friend")
    public Page<PostOutput> getPostOfFriendProfile(@RequestHeader("Authorization") String accessToken,
                                                   @RequestParam Long friendId,
                                                   @ParameterObject Pageable pageable){
        return postService.getPostOfListFriend(accessToken, friendId, pageable);
    }

    @Operation(summary = "Lấy post của người lạ")
    @GetMapping("/list/post-user")
    public Page<PostOutput> getPostOfUserProfile(@RequestHeader("Authorization") String accessToken,
                                                 @RequestParam Long userId,
                                                 @ParameterObject Pageable pageable){
        return postService.getPostsByUserId(userId, accessToken, pageable);
    }
}
