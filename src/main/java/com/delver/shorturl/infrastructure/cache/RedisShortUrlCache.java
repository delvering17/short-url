package com.delver.shorturl.infrastructure.cache;

import com.delver.shorturl.application.ShortUrlCache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Slf4j
@Component
public class RedisShortUrlCache implements ShortUrlCache {

    private static final String KEY_PREFIX = "shorturl:destination:";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public RedisShortUrlCache(StringRedisTemplate redisTemplate, @Value("${short-url.cache.ttl}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    @Override
    public @Nullable String findOriginalUrl(String shortCode) {
        try {
            return redisTemplate.opsForValue().get(KEY_PREFIX + shortCode);
        } catch (DataAccessException e) {
            log.debug("Redis 원본 URL 캐시 조회 실패 (shortCode={})", shortCode, e);
            return null;
        }
    }

    @Override
    public void put(String shortCode, String originalUrl) {
        writeCache("저장", shortCode, () -> redisTemplate.opsForValue().set(KEY_PREFIX + shortCode, originalUrl, ttl));
    }

    @Override
    public void putAfterCommit(String shortCode, String originalUrl) {
        runAfterCommit(() -> put(shortCode, originalUrl));
    }

    @Override
    public void evictAfterCommit(String shortCode) {
        runAfterCommit(() -> writeCache("삭제", shortCode, () -> redisTemplate.delete(KEY_PREFIX + shortCode)));
    }

    private void writeCache(String operation, String shortCode, Runnable action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            log.debug("Redis 원본 URL 캐시 {} 실패 (shortCode={})", operation, shortCode, e);
        }
    }

    private void runAfterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
