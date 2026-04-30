package com.aanya.coreapi.service;

import com.aanya.coreapi.dto.CreateCommentRequest;
import com.aanya.coreapi.entity.Comment;
import com.aanya.coreapi.entity.Post;
import com.aanya.coreapi.exception.DepthLimitException;
import com.aanya.coreapi.exception.RateLimitException;
import com.aanya.coreapi.repository.CommentRepository;
import com.aanya.coreapi.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_BOT_REPLIES = 100;
    private static final int MAX_DEPTH = 20;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RedisGuardrailService redis;
    private final NotificationService notificationService;

    @Transactional
    public Comment addComment(Long postId, CreateCommentRequest req) {

        // 1. Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        // 2. Compute depth
        int depth = computeDepth(req.getParentCommentId());

        // 3. Depth limit
        if (depth > MAX_DEPTH) {
            throw new DepthLimitException(
                    "Comment thread has reached maximum depth of " + MAX_DEPTH);
        }

        boolean isCooldown = false;

        // 4. Bot guardrails
        if (req.getAuthorType() == Post.AuthorType.BOT) {
            isCooldown = applyBotGuardrails(postId, req.getAuthorId(), req.getHumanUserId());
        }

        // 5. Save comment (ALWAYS)
        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(req.getAuthorId())
                .authorType(req.getAuthorType())
                .content(req.getContent())
                .depthLevel(depth)
                .parentCommentId(req.getParentCommentId())
                .build();

        comment = commentRepository.save(comment);

        // 6. Virality + Notification
        if (req.getAuthorType() == Post.AuthorType.BOT) {

            redis.incrementVirality(postId, 1);

            // ✅ Only notify if NOT on cooldown
            if (!isCooldown && req.getHumanUserId() != null) {
                notificationService.handleBotInteractionNotification(
                        req.getHumanUserId(),
                        "Bot " + req.getAuthorId() + " replied to your post"
                );
            }

        } else {
            redis.incrementVirality(postId, 50);
        }

        log.info("Comment added to post {}. Depth: {}. Author type: {}. Cooldown: {}. Virality: {}",
                postId, depth, req.getAuthorType(), isCooldown, redis.getViralityScore(postId));

        return comment;
    }

    /**
     * BOT GUARDRAILS
     * @return true if cooldown is active
     */
    private boolean applyBotGuardrails(Long postId, Long botId, Long humanUserId) {

        // Horizontal cap
        boolean allowed = redis.tryIncrementBotCount(postId);
        if (!allowed) {
            throw new RateLimitException(
                    "Post " + postId + " has reached the maximum of " +
                            MAX_BOT_REPLIES + " bot replies.");
        }

        // Cooldown check
        if (humanUserId != null) {
            boolean onCooldown = redis.isBotOnCooldown(botId, humanUserId);

            if (onCooldown) {
                log.warn("Bot {} is on cooldown for user {}. Skipping notification.",
                        botId, humanUserId);


                return true;
            }
        }

        return false;
    }

    /**
     * DEPTH CALCULATION
     */
    private int computeDepth(Long parentCommentId) {
        if (parentCommentId == null) return 0;

        return commentRepository.findById(parentCommentId)
                .map(parent -> parent.getDepthLevel() + 1)
                .orElseThrow(() ->
                        new IllegalArgumentException("Parent comment not found: " + parentCommentId));
    }
}