package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserOnlineStatusChangedEvent;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

  private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
  private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access_tokens";
  private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh_tokens";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

  private final int maxActiveJwtCount;
  private final ApplicationEventPublisher eventPublisher;
  private final RedisTemplate<String, Object> jwtRedisTemplate;
  private final RedisLockProvider redisLockProvider;

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    String userKey = getUserKey(jwtInformation.userId());
    String lockKey = jwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      Long sizeBefore = jwtRedisTemplate.opsForList().size(userKey);
      boolean wasOffline = (sizeBefore == null || sizeBefore == 0);

      jwtRedisTemplate.opsForList().rightPush(userKey, jwtInformation);
      jwtRedisTemplate.expire(userKey, DEFAULT_TTL);
      addTokenIndex(jwtInformation);

      Long currentSize = jwtRedisTemplate.opsForList().size(userKey);
      while (currentSize != null && currentSize > maxActiveJwtCount) {
        Object oldest = jwtRedisTemplate.opsForList().leftPop(userKey);
        if (oldest instanceof JwtInformation oldestInfo) {
          removeTokenIndex(oldestInfo);
        }
        currentSize = jwtRedisTemplate.opsForList().size(userKey);
      }

      if (wasOffline) {
        eventPublisher.publishEvent(
            new UserOnlineStatusChangedEvent(jwtInformation.userId(), true));
      }
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);

    List<Object> tokens = jwtRedisTemplate.opsForList().range(userKey, 0, -1);
    boolean wasOnline = tokens != null && !tokens.isEmpty();

    if (tokens != null) {
      tokens.forEach(tokenObj -> {
        if (tokenObj instanceof JwtInformation info) {
          removeTokenIndex(info);
        }
      });
    }

    jwtRedisTemplate.delete(userKey);

    if (wasOnline) {
      eventPublisher.publishEvent(new UserOnlineStatusChangedEvent(userId, false));
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Long size = jwtRedisTemplate.opsForList().size(getUserKey(userId));
    return size != null && size > 0;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return Boolean.TRUE.equals(
        jwtRedisTemplate.opsForSet().isMember(ACCESS_TOKEN_INDEX_KEY, accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(
        jwtRedisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken));
  }

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    String userKey = getUserKey(newJwtInformation.userId());
    String lockKey = newJwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      List<Object> tokens = jwtRedisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation info
              && info.refreshToken().equals(oldRefreshToken)) {
            removeTokenIndex(info);
            jwtRedisTemplate.opsForList().set(userKey, i, newJwtInformation);
            addTokenIndex(newJwtInformation);
            jwtRedisTemplate.expire(userKey, DEFAULT_TTL);
            return newJwtInformation;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }

    // 회전 대상(oldRefreshToken)을 못 찾은 경우 새 토큰으로 등록 (폴백)
    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  public Set<UUID> getActiveUserIds() {
    Set<String> keys = jwtRedisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
    if (keys == null) {
      return Set.of();
    }
    return keys.stream()
        .map(key -> UUID.fromString(key.substring(USER_JWT_KEY_PREFIX.length())))
        .collect(Collectors.toSet());
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Set<String> userKeys = jwtRedisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
    if (userKeys == null) {
      return;
    }

    Instant now = Instant.now();

    for (String userKey : userKeys) {
      List<Object> tokens = jwtRedisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens == null || tokens.isEmpty()) {
        continue;
      }

      boolean stillHasValid = false;

      for (Object tokenObj : tokens) {
        if (tokenObj instanceof JwtInformation info) {
          if (info.expiration().isBefore(now)) {
            jwtRedisTemplate.opsForList().remove(userKey, 1, info);
            removeTokenIndex(info);
          } else {
            stillHasValid = true;
          }
        }
      }

      if (!stillHasValid) {
        jwtRedisTemplate.delete(userKey);
        String userIdStr = userKey.substring(USER_JWT_KEY_PREFIX.length());
        eventPublisher.publishEvent(
            new UserOnlineStatusChangedEvent(UUID.fromString(userIdStr), false));
      }
    }
  }

  private String getUserKey(UUID userId) {
    return USER_JWT_KEY_PREFIX + userId;
  }

  private void addTokenIndex(JwtInformation info) {
    jwtRedisTemplate.opsForSet().add(ACCESS_TOKEN_INDEX_KEY, info.accessToken());
    jwtRedisTemplate.opsForSet().add(REFRESH_TOKEN_INDEX_KEY, info.refreshToken());
    jwtRedisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, DEFAULT_TTL);
    jwtRedisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, DEFAULT_TTL);
  }

  private void removeTokenIndex(JwtInformation info) {
    jwtRedisTemplate.opsForSet().remove(ACCESS_TOKEN_INDEX_KEY, info.accessToken());
    jwtRedisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, info.refreshToken());
  }
}
