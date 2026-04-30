package com.aanya.coreapi.dto;

import com.aanya.coreapi.entity.Post;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;

    private Long postId;

    private Long authorId;

    private Post.AuthorType authorType;

    private String content;

    private int depthLevel;

    private Long parentCommentId;

    private LocalDateTime createdAt;
}