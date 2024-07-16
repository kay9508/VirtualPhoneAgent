package com.sinyoung.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate redisTemplate;

    public void saveHash(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public String getHash(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    public boolean hasHash(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }
}
