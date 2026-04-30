package com.aanya.coreapi.dto;

import com.aanya.coreapi.entity.Post;
import lombok.Data;

@Data
public class CreatePostRequest {
    private Long authorId;
    private Post.AuthorType authorType;
    private String content;
}