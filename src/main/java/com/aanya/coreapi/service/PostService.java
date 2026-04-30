package com.aanya.coreapi.service;

import com.aanya.coreapi.dto.CreatePostRequest;
import com.aanya.coreapi.dto.PostResponse;
import com.aanya.coreapi.entity.Post;
import com.aanya.coreapi.repository.PostRepository;
import com.aanya.coreapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisGuardrailService  redis;
    private final NotificationService    notificationService;

    // ─── Create Post ──────────────────────────────────────────────────────────

    @Transactional
    public PostResponse createPost(CreatePostRequest req) {
        // Validate author exists
        validateAuthor(req.getAuthorId(), req.getAuthorType());

        Post post = Post.builder()
                .authorId(req.getAuthorId())
                .authorType(req.getAuthorType())
                .content(req.getContent())
                .build();

        post = postRepository.save(post);
        Long viralityScore = redis.getViralityScore(post.getId());

        return toResponse(post, viralityScore);
    }

    // ─── Like a Post ──────────────────────────────────────────────────────────

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // Human Like = +20 virality
        redis.incrementVirality(postId, 20);

        // Notify post author if it's a different user
        if (post.getAuthorType() == Post.AuthorType.USER &&
                !post.getAuthorId().equals(userId)) {

            notificationService.handleBotInteractionNotification(
                    post.getAuthorId(),
                    "User " + userId + " liked your post"
            );
        }
        log.info("Post {} liked by user {}. Virality: {}", postId, userId,
                redis.getViralityScore(postId));
    }

    private void validateAuthor(Long authorId, Post.AuthorType authorType) {
        if (authorType == Post.AuthorType.USER) {
            userRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorId));
        }
        // Bot validation can be added similarly
    }

    private PostResponse toResponse(Post post, Long viralityScore) {
        return PostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorType(post.getAuthorType().name())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .viralityScore(viralityScore)
                .build();
    }
}