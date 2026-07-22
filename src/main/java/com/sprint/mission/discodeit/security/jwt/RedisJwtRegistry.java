package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
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
  private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access_tokens";
  private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh_tokens";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockProvider redisLockProvider;

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10, backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    String userKey = getUserKey(jwtInformation.userId());
    String lockKey = jwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      Long currentSize = redisTemplate.opsForList().size(userKey);

      while (currentSize != null && currentSize >= maxActiveJwtCount) {
        Object oldestTokenObj = redisTemplate.opsForList().leftPop(userKey);
        if (oldestTokenObj instanceof JwtInformation oldestToken) {
          removeTokenIndex(oldestToken.accessToken(), oldestToken.refreshToken());
        }
        currentSize = redisTemplate.opsForList().size(userKey);
      }

      redisTemplate.opsForList().rightPush(userKey, jwtInformation);
      redisTemplate.expire(userKey, DEFAULT_TTL);
      addTokenIndex(jwtInformation.accessToken(), jwtInformation.refreshToken());

    } finally {
      redisLockProvider.releaseLock(lockKey);
    }
  }

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);

    List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
    if (tokens != null) {
      tokens.forEach(tokenObj -> {
        if (tokenObj instanceof JwtInformation jwtInfo) {
          removeTokenIndex(jwtInfo.accessToken(), jwtInfo.refreshToken());
        }
      });
    }
    redisTemplate.delete(userKey);
  }

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      return;
    }

    // 빠른 검증 차단을 위해 인덱스에서 먼저 제거
    redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);

    Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
    if (userKeys != null) {
      for (String userKey : userKeys) {
        List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
        if (tokens != null) {
          for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof JwtInformation jwtInfo && jwtInfo.refreshToken()
                .equals(refreshToken)) {
              redisTemplate.opsForList().set(userKey, i, "EXPIRED");
              redisTemplate.opsForList().remove(userKey, 1, "EXPIRED");
              removeTokenIndex(jwtInfo.accessToken(), jwtInfo.refreshToken());
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);
    Long size = redisTemplate.opsForList().size(userKey);
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

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10, backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    String userKey = getUserKey(newJwtInformation.userId());
    String lockKey = newJwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);
    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation jwtInfo && jwtInfo.refreshToken()
              .equals(oldRefreshToken)) {

            removeTokenIndex(jwtInfo.accessToken(), jwtInfo.refreshToken());

            // 기존 객체 교체
            redisTemplate.opsForList().set(userKey, i, newJwtInformation);
            addTokenIndex(newJwtInformation.accessToken(), newJwtInformation.refreshToken());
            redisTemplate.expire(userKey, DEFAULT_TTL);
            break;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }
    return newJwtInformation;
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
    if (userKeys == null) {
      return;
    }

    for (String userKey : userKeys) {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

      if (tokens != null) {
        boolean hasValidTokens = false;

        for (int i = tokens.size() - 1; i >= 0; i--) {
          if (tokens.get(i) instanceof JwtInformation jwtInfo) {
            boolean isExpired =
                !jwtTokenProvider.validateToken(jwtInfo.accessToken()) ||
                    !jwtTokenProvider.validateToken(jwtInfo.refreshToken());

            if (isExpired) {
              redisTemplate.opsForList().set(userKey, i, "EXPIRED");
              redisTemplate.opsForList().remove(userKey, 1, "EXPIRED");
              removeTokenIndex(jwtInfo.accessToken(), jwtInfo.refreshToken());
            } else {
              hasValidTokens = true;
            }
          }
        }

        if (!hasValidTokens) {
          redisTemplate.delete(userKey);
        }
      }
    }
  }

  private String getUserKey(UUID userId) {
    return USER_JWT_KEY_PREFIX + userId.toString();
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