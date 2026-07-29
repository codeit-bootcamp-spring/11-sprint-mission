package com.sprint.mission.discodeit.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockProvider {

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    private static final String LOCK_KEY_PREFIX = "lock:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void acquireLock(String key) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                LOCK_KEY_PREFIX + key,
                Thread.currentThread().getName(),
                LOCK_TIMEOUT
        );

        if (!Boolean.TRUE.equals(acquired)) {
            throw new RedisLockAcquisitionException(
                    "분산 락 획득에 실패했습니다. key=" + key
            );
        }
    }

    public void releaseLock(String key) {
        redisTemplate.delete(LOCK_KEY_PREFIX + key);
    }

    public static class RedisLockAcquisitionException extends RuntimeException {

        public RedisLockAcquisitionException(String message) {
            super(message);
        }
    }
}
