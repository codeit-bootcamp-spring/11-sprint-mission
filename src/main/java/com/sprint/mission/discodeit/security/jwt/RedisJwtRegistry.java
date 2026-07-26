package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

  private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
  private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access-tokens";
  private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh-tokens";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockProvider redisLockProvider;

  @Retryable(retryFor = RedisLockAcquisitionException.class,
      maxAttemptsExpression = "${discodeit.redis.lock.max-attempts:10}",
      backoff = @Backoff(delayExpression = "${discodeit.redis.lock.delay:100}",
          multiplierExpression = "${discodeit.redis.lock.multiplier:2}"))
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserResponse().id();
    String userKey = getUserKey(userId);
    String lockKey = userId.toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      Long currentSize = redisTemplate.opsForList().size(userKey);
      while (currentSize != null && currentSize >= maxActiveJwtCount) {
        Object oldest = redisTemplate.opsForList().leftPop(userKey);
        if (oldest instanceof JwtInformation oldestInfo) {
          removeTokenIndex(oldestInfo.getAccessToken(), oldestInfo.getRefreshToken());
        }
        currentSize = redisTemplate.opsForList().size(userKey);
      }

      redisTemplate.opsForList().rightPush(userKey, jwtInformation);
      redisTemplate.expire(userKey, DEFAULT_TTL);
      addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }

    log.info("redis jwt register success: userId={}", userId);
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);

    List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
    if (tokens != null) {
      tokens.forEach(tokenObj -> {
        if (tokenObj instanceof JwtInformation jwtInfo) {
          removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
        }
      });
    }

    redisTemplate.delete(userKey);
    log.info("redis jwt invalidate success: userId={}", userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Long size = redisTemplate.opsForList().size(getUserKey(userId));
    return size != null && size > 0;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return Boolean.TRUE.equals(
        redisTemplate.opsForSet().isMember(ACCESS_TOKEN_INDEX_KEY, accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(
        redisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken));
  }

  @Retryable(retryFor = RedisLockAcquisitionException.class,
      maxAttemptsExpression = "${discodeit.redis.lock.max-attempts:10}",
      backoff = @Backoff(delayExpression = "${discodeit.redis.lock.delay:100}",
          multiplierExpression = "${discodeit.redis.lock.multiplier:2}"))
  @Override
  public void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.getUserResponse().id();
    String userKey = getUserKey(userId);
    String lockKey = userId.toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation jwtInfo
              && jwtInfo.getRefreshToken().equals(oldRefreshToken)) {
            removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
            jwtInfo.rotate(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken(),
                newJwtInformation.getExpiration());
            redisTemplate.opsForList().set(userKey, i, jwtInfo);
            addTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
            redisTemplate.expire(userKey, DEFAULT_TTL);
            break;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }
  }

  @Scheduled(fixedDelayString = "${discodeit.jwt.cleanup-interval}")
  @Override
  public void clearExpiredJwtInformation() {
    Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
    if (userKeys == null) {
      return;
    }

    for (String userKey : userKeys) {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens == null) {
        continue;
      }

      boolean hasValidTokens = false;
      for (Object tokenObj : tokens) {
        if (!(tokenObj instanceof JwtInformation jwtInfo)) {
          continue;
        }
        boolean expired = !jwtTokenProvider.validateAccessToken(jwtInfo.getAccessToken())
            || !jwtTokenProvider.validateRefreshToken(jwtInfo.getRefreshToken());
        if (expired) {
          redisTemplate.opsForList().remove(userKey, 1, tokenObj);
          removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
        } else {
          hasValidTokens = true;
        }
      }

      if (!hasValidTokens) {
        redisTemplate.delete(userKey);
      }
    }
  }

  private String getUserKey(UUID userId) {
    return USER_JWT_KEY_PREFIX + userId;
  }

  private void addTokenIndex(String accessToken, String refreshToken) {
    redisTemplate.opsForSet().add(ACCESS_TOKEN_INDEX_KEY, accessToken);
    redisTemplate.opsForSet().add(REFRESH_TOKEN_INDEX_KEY, refreshToken);
    redisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, DEFAULT_TTL);
    redisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, DEFAULT_TTL);
  }

  private void removeTokenIndex(String accessToken, String refreshToken) {
    redisTemplate.opsForSet().remove(ACCESS_TOKEN_INDEX_KEY, accessToken);
    redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);
  }
}