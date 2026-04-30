package com.aanya.coreapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisGuardrailService {

    private final RedisTemplate<String, String> redisTemplate;


    public String viralityKey(Long postId) {
        return "post:" + postId + ":virality_score";
    }

    private String botCountKey(Long postId) {
        return "post:" + postId + ":bot_count";
    }

    private String cooldownKey(Long botId, Long humanId) {
        return "cooldown:bot:" + botId + ":user:" + humanId;
    }

    private String notifCooldownKey(Long userId) {
        return "notif:cooldown:user:" + userId;
    }

    public String pendingNotifsKey(Long userId) {
        return "user:" + userId + ":pending_notifs";
    }


    public void incrementVirality(Long postId, int points) {
        redisTemplate.opsForValue().increment(viralityKey(postId), points);
    }

    public Long getViralityScore(Long postId) {
        String val = redisTemplate.opsForValue().get(viralityKey(postId));
        return val == null ? 0L : Long.parseLong(val);
    }


    public boolean tryIncrementBotCount(Long postId) {
        Long newCount = redisTemplate.opsForValue().increment(botCountKey(postId));

        if (newCount != null && newCount <= 100) {
            return true;
        }

        // rollback if exceeded
        redisTemplate.opsForValue().decrement(botCountKey(postId));
        return false;
    }

    public Long getBotCount(Long postId) {
        String val = redisTemplate.opsForValue().get(botCountKey(postId));
        return val == null ? 0L : Long.parseLong(val);
    }

    public void tryDecrBotCount(Long postId) {
        Long current = getBotCount(postId);
        if (current != null && current > 0) {
            redisTemplate.opsForValue().decrement(botCountKey(postId));
        }
    }





    public boolean isUserOnNotifCooldown(Long userId) {
        String key = notifCooldownKey(userId);

        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofMinutes(15));

        return Boolean.FALSE.equals(wasSet);
    }



    public void pushPendingNotification(Long userId, String message) {
        redisTemplate.opsForList().rightPush(pendingNotifsKey(userId), message);
    }

    public List<String> popAllPendingNotifications(Long userId) {
        String key = pendingNotifsKey(userId);

        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) return List.of();

        List<String> messages = redisTemplate.opsForList().range(key, 0, size - 1);

        redisTemplate.delete(key);

        return messages == null ? List.of() : messages;
    }

    public Set<String> getAllPendingNotifKeys() {
        return redisTemplate.keys("user:*:pending_notifs");
    }
    public boolean isBotOnCooldown(Long botId, Long userId) {
        String key = "bot:cooldown:" + botId + ":" + userId;

        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            return true; // already on cooldown
        }

        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(10));
        return false;
    }
}