package com.example.chatAppServer.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PresenceService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_NAME = "presence";

    @Autowired
    public PresenceService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void plusCode(String userId, String code) {
        String key = CACHE_NAME + ":" + userId;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
    }

    public String getCode(String userId) {
        String key = CACHE_NAME + ":" + userId;
        Object amountSession = redisTemplate.opsForValue().get(key);
        return (String) amountSession;
    }

    public void delete(String userId) {
        String key = CACHE_NAME + ":" + userId;
        redisTemplate.delete(key);
    }
}
