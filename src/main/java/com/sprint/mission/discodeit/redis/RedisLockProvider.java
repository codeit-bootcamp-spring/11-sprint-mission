package com.sprint.mission.discodeit.redis;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockProvider {

  private static final String LOCK_KEY_PREFIX = "lock:";
  private static final Duration LOCK_TTL = Duration.ofSeconds(3);

  private final StringRedisTemplate stringRedisTemplate;
  private final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();

  public void acquireLock(String key) {
    String localKey = LOCK_KEY_PREFIX + key;
    String lockValue = UUID.randomUUID().toString();

    Boolean acquired = stringRedisTemplate.opsForValue()
        .setIfAbsent(localKey, lockValue, LOCK_TTL);

    if (!Boolean.TRUE.equals(acquired)) {
      throw new RedisLockAcquisitionException(key);
    }
    lockValueHolder.set(lockValue);
  }

  public void releaseLock(String key) {
    String lockKey = LOCK_KEY_PREFIX + key;
    String lockValue = lockValueHolder.get();
    try {
      // 자신이 건 락인지 확인 후 해제 (TTL 만료 후 다른 스레드가 잡은 락을 실수로 풀지 않기 위함)
      if (lockValue != null && lockValue.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
        stringRedisTemplate.delete(lockKey);
      }
    } finally {
      lockValueHolder.remove();
    }
  }

  public static class RedisLockAcquisitionException extends RuntimeException {

    public RedisLockAcquisitionException(String key) {
      super("Redis 락 획득 실패 - key: " + key);
    }
  }
}
