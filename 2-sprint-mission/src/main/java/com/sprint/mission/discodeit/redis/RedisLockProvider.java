package com.sprint.mission.discodeit.redis;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockProvider {

  private static final String LOCK_PREFIX = "lock:jwt:";
  private static final Duration LOCK_TTL = Duration.ofSeconds(3);

  private final RedisTemplate<String, Object> redisTemplate;

  public String acquireLock(String key) {
    String lockKey = LOCK_PREFIX + key;
    String lockValue = UUID.randomUUID().toString();

    Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, lockValue, LOCK_TTL);

    if (Boolean.TRUE.equals(success)) {
      return lockValue;
    }
    throw new RedisLockAcquisitionException(key);
  }

  public void releaseLock(String key, String lockValue) {
    String lockKey = LOCK_PREFIX + key;
    Object current = redisTemplate.opsForValue().get(lockKey);
    
    if (lockValue.equals(current)) {
      redisTemplate.delete(lockKey);
    }
  }

  public static class RedisLockAcquisitionException extends RuntimeException {

    public RedisLockAcquisitionException(String key) {
      super("락 획득 실패: " + key);
    }
  }
}