package com.aanya.coreapi.schedular;


import com.aanya.coreapi.service.RedisGuardrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RedisGuardrailService redis;

    /**
     * Runs every 5 minutes.
     * Scans all users with pending notifications, aggregates, logs, clears.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in ms
    public void sweepPendingNotifications() {
        log.info("[CRON] Starting notification sweep...");

        Set<String> keys = redis.getAllPendingNotifKeys();
        if (keys == null || keys.isEmpty()) {
            log.info("[CRON] No pending notifications found.");
            return;
        }

        for (String key : keys) {
            // Extract userId from key pattern "user:{id}:pending_notifs"
            String[] parts = key.split(":");
            if (parts.length < 3) continue;

            Long userId = Long.parseLong(parts[1]);
            List<String> messages = redis.popAllPendingNotifications(userId);

            if (messages.isEmpty()) continue;

            // Build summary: "Bot X and [N-1] others interacted with your posts."
            String first = messages.get(0);
            int others = messages.size() - 1;

            String summary = others > 0
                    ? "Summarized Push Notification: " + first + " and [" + others + "] others interacted with your posts."
                    : "Summarized Push Notification: " + first;

            log.info("[CRON] User {}: {}", userId, summary);
        }

        log.info("[CRON] Sweep complete. Processed {} users.", keys.size());
    }
}