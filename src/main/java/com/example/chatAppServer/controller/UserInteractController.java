package com.example.chatAppServer.controller;

import com.example.chatAppServer.dto.post.PostInput;
import com.example.chatAppServer.dto.post.PostOutput;
import com.example.chatAppServer.dto.user.UserOutput;
import com.example.chatAppServer.service.UserInteractService;
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
@RequestMapping("api/v1/user/post/interaction")
@AllArgsConstructor
@CrossOrigin
public class UserInteractController {
    private final UserInteractService userInteractService;

    @Operation(summary = "Thích bài viết của bạn bè")
    @PostMapping("/like")
    public void like(@RequestParam Long postId, @RequestHeader("Authorization") String accessToken){
        userInteractService.likePost(postId, accessToken);
    }

    @Operation(summary = "Bỏ thích bài viết của bạn bè")
    @DeleteMapping("/remove-like")
    public void removeLike(@RequestParam Long postId, @RequestHeader("Authorization") String accessToken){
        userInteractService.deleteLike(postId, accessToken);
    }

    @Operation(summary = "Bình luận bài viết của bạn bè")
    @PostMapping("/comment")
    public void comment(@RequestParam Long postId,
                        @RequestPart @Valid String comment,
                        @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles,
                        @RequestHeader("Authorization") String accessToken){
        userInteractService.commentPost(postId, accessToken, comment, multipartFiles);
    }

    @Operation(summary = "Cập nhật comment")
    @PostMapping("/update-comment")
    public void updateComment(@RequestParam Long postId,
                              @RequestPart @Valid String comment,
                              @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles,
                              @RequestHeader("Authorization") String accessToken) {
        userInteractService.updateComment(postId, accessToken, comment, multipartFiles);
    }

    @Operation(summary = "Xóa bình luận bài viết của bạn bè")
    @DeleteMapping("/comment/delete")
    public void removeComment(@RequestParam  Long commentId,
                              @RequestHeader("Authorization") String accessToken){
        userInteractService.deleteComment(commentId, accessToken);
    }

    @Operation(summary = "Danh sách người thích bài viết")
    @GetMapping("/like/list")
    public Page<UserOutput> getUsersLikeOfPost(@RequestParam Long postId,
                                               @ParameterObject Pageable pageable){
        return userInteractService.getUsersLikeOfPost(postId, pageable);
    }

    @Operation(summary = "Chi tiết về bài viết (gồm thông tin chi tiết + comment)")
    @GetMapping
    public PostOutput getPostAndComment(@RequestParam Long postId,
                                        @RequestHeader("Authorization") String accessToken){
        return userInteractService.getPostAndComment(postId, accessToken);
    }

    @Operation(summary = "Chia sẻ bài viết")
    @PostMapping("/share")
    public void sharePost(@RequestHeader("Authorization") String accessToken,
                          @RequestParam Long shareId,
                          @RequestBody @Valid PostInput sharePostInput){
        userInteractService.sharePost(accessToken, shareId, sharePostInput);
    }
}
