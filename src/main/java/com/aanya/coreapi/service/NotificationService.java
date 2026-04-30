package com.aanya.coreapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisGuardrailService redis;


    public void handleBotInteractionNotification(Long userId, String message) {

        boolean onCooldown = redis.isUserOnNotifCooldown(userId);

        if (onCooldown) {
            redis.pushPendingNotification(userId, message);
            log.info("[Notif] User {} is on cooldown. Queued: '{}'", userId, message);
        } else {
            log.info("[Notif] Push Notification Sent to User {}: '{}'", userId, message);
        }
    }
}