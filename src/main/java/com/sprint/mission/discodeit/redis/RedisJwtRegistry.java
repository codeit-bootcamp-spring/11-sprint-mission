package com.sprint.mission.discodeit.redis;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

  private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
  private static final String ACCESS_TOKEN_INDEX_PREFIX = "jwt:index:access:";
  private static final String REFRESH_TOKEN_INDEX_PREFIX = "jwt:index:refresh:";

  private final int maxActiveJwtCount;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockProvider redisLockProvider;

  @CacheEvict(value = CacheConfig.USERS, key = "#jwtInformation.userDto().id()")
  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10, backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.userDto().id();
    String userKey = getUserKey(userId);

    redisLockProvider.acquireLock(userId.toString());
    try {
      Long currentSize = redisTemplate.opsForList().size(userKey);

      while (currentSize != null && currentSize >= maxActiveJwtCount) {
        Object oldest = redisTemplate.opsForList().leftPop(userKey);
        if (oldest instanceof JwtInformation oldestInfo) {
          removeTokenIndex(oldestInfo.accessToken(), oldestInfo.refreshToken());
        }
        currentSize = redisTemplate.opsForList().size(userKey);
      }

      redisTemplate.opsForList().rightPush(userKey, jwtInformation);

      long ttlSeconds = Math.max(1,
          jwtInformation.expiration().getEpochSecond() - Instant.now().getEpochSecond());
      redisTemplate.expire(userKey, Duration.ofSeconds(ttlSeconds));

      addTokenIndex(jwtInformation.accessToken(), jwtInformation.refreshToken(), userId,
          ttlSeconds);
    } finally {
      redisLockProvider.releaseLock(userId.toString());
    }
  }

  @CacheEvict(value = CacheConfig.USERS, key = "#userId")
  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);

    List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
    if (tokens != null) {
      tokens.forEach(t -> {
        if (t instanceof JwtInformation info) {
          removeTokenIndex(info.accessToken(), info.refreshToken());
        }
      });
    }
    redisTemplate.delete(userKey);
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    Object userIdObj = redisTemplate.opsForValue().get(REFRESH_TOKEN_INDEX_PREFIX + refreshToken);
    if (userIdObj == null) {
      return;
    }

    UUID userId = UUID.fromString(userIdObj.toString());
    String userKey = getUserKey(userId);

    redisLockProvider.acquireLock(userId.toString());
    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens != null) {
        for (Object t : tokens) {
          if (t instanceof JwtInformation info && info.refreshToken().equals(refreshToken)) {
            redisTemplate.opsForList().remove(userKey, 1, info);
            removeTokenIndex(info.accessToken(), info.refreshToken());
            break;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(userId.toString());
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Long size = redisTemplate.opsForList().size(getUserKey(userId));
    return size != null && size > 0;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return redisTemplate.hasKey(ACCESS_TOKEN_INDEX_PREFIX + accessToken);
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return redisTemplate.hasKey(REFRESH_TOKEN_INDEX_PREFIX + refreshToken);
  }

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10, backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.userDto().id();
    String userKey = getUserKey(userId);

    redisLockProvider.acquireLock(userId.toString());
    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation info
              && info.refreshToken().equals(oldRefreshToken)) {
            removeTokenIndex(info.accessToken(), info.refreshToken());
            redisTemplate.opsForList().set(userKey, i, newJwtInformation);

            long ttlSeconds = Math.max(1,
                newJwtInformation.expiration().getEpochSecond() - Instant.now().getEpochSecond());
            addTokenIndex(newJwtInformation.accessToken(), newJwtInformation.refreshToken(),
                userId, ttlSeconds);
            redisTemplate.expire(userKey, Duration.ofSeconds(ttlSeconds));
            break;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(userId.toString());
    }
    return newJwtInformation;
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    // record에 expiration 필드가 이미 있어서 JWT를 재검증 할 필요 없음
  }

  private String getUserKey(UUID userId) {
    return USER_JWT_KEY_PREFIX + userId;
  }

  private void removeTokenIndex(String accessToken, String refreshToken) {
    redisTemplate.delete(ACCESS_TOKEN_INDEX_PREFIX + accessToken);
    redisTemplate.delete(REFRESH_TOKEN_INDEX_PREFIX + refreshToken);
  }

  private void addTokenIndex(String accessToken, String refreshToken, UUID userId,
      long ttlSeconds) {
    redisTemplate.opsForValue()
        .set(ACCESS_TOKEN_INDEX_PREFIX + accessToken, userId, Duration.ofSeconds(ttlSeconds));
    redisTemplate.opsForValue()
        .set(REFRESH_TOKEN_INDEX_PREFIX + refreshToken, userId, Duration.ofSeconds(ttlSeconds));
  }


}
