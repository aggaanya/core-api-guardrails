package com.aanya.coreapi.dto;


import com.aanya.coreapi.entity.Post;
import lombok.Data;

@Data
public class CreateCommentRequest {
    private Long authorId;
    private Post.AuthorType authorType;
    private String content;
    private Long parentCommentId; // null for top-level
    private Long humanUserId;     // required when authorType = BOT (for cooldown check)
}