package com.aanya.coreapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// PostResponse.java
@Data
@Builder
public class PostResponse {
    private Long id;
    private Long authorId;
    private String authorType;
    private String content;
    private LocalDateTime createdAt;
    private Long viralityScore;
}