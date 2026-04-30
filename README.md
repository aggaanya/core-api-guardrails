# core-api-guardrails
Spring Boot backend implementing post &amp; comment system with Redis-based guardrails (rate limiting, cooldown, and virality tracking).
# Core API Guardrails

## 🚀 Overview
This project is a Spring Boot backend system that implements a **post & comment platform with guardrails** to control bot activity using Redis.

It demonstrates real-world backend design concepts like:
- Rate limiting
- Cooldown mechanisms
- Depth control
- Notification throttling
- Redis-based atomic operations

---

## 🧠 Key Features

### 📝 Post & Comment System
- Users can create posts
- Users and bots can add comments
- Supports nested comments using `parentCommentId`

---

### 🛡️ Guardrails (Core Logic)

#### 1. Horizontal Limit (Bot Rate Limiting)
- Max **100 bot replies per post**
- Prevents bot flooding

#### 2. Vertical Limit (Depth Control)
- Max **depth = 20**
- Prevents deep recursion / spam threads

#### 3. Cooldown Mechanism
- A bot cannot repeatedly notify the same user within a short time
- Uses Redis TTL keys
- Behavior:
  - First interaction → notification sent
  - Subsequent interactions → notification suppressed (but comment still saved)

#### 4. Notification Control
- Notifications are sent only when not in cooldown
- During cooldown → notifications are skipped (not thrown as errors)

---

### 📊 Virality Score
- BOT comment → +1
- USER comment → +50  
- Stored in Redis for fast updates

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Redis
- Docker
- Postman (API testing)

---

## 🐳 Running the Project

### 1. Start Services
```bash
docker-compose up -d
