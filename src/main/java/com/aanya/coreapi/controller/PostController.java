package com.aanya.coreapi.controller;


import com.aanya.coreapi.dto.CreatePostRequest;
import com.aanya.coreapi.dto.PostResponse;
import com.aanya.coreapi.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(req));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @RequestParam Long userId) {

        postService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }
}