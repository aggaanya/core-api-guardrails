package com.aanya.coreapi.controller;

import com.aanya.coreapi.dto.CommentResponse;
import com.aanya.coreapi.dto.CreateCommentRequest;
import com.aanya.coreapi.entity.Comment;
import com.aanya.coreapi.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
        Comment savedComment = commentService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(savedComment));
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorType(comment.getAuthorType())
                .content(comment.getContent())
                .depthLevel(comment.getDepthLevel())
                .parentCommentId(comment.getParentCommentId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}