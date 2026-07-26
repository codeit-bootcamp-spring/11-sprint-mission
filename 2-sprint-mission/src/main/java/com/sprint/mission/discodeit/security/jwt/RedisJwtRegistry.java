package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
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

  private static final String USER_KEY_PREFIX = "jwt:user:";
  private static final String ACCESS_INDEX = "jwt:access_tokens";
  private static final String REFRESH_INDEX = "jwt:refresh_tokens";
  private static final String REMOVAL_MARKER = "__REMOVED__";

  private final int maxActiveJwtCount;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockProvider redisLockProvider;

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserDto().id();
    String userKey = userKey(userId);
    String lockValue = redisLockProvider.acquireLock(userId.toString());

    try {
      Long size = redisTemplate.opsForList().size(userKey);
      while (size != null && size >= maxActiveJwtCount) {
        Object evicted = redisTemplate.opsForList().leftPop(userKey);
        if (evicted instanceof JwtInformation info) {
          removeIndex(info.getAccessToken(), info.getRefreshToken());
        }
        size = redisTemplate.opsForList().size(userKey);
      }

      redisTemplate.opsForList().rightPush(userKey, jwtInformation);
      redisTemplate.expireAt(userKey, jwtInformation.getExpiration());
      addIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
    } finally {
      redisLockProvider.releaseLock(userId.toString(), lockValue);
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userKey = userKey(userId);
    List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
    if (tokens != null) {
      tokens.forEach(t -> {
        if (t instanceof JwtInformation info) {
          removeIndex(info.getAccessToken(), info.getRefreshToken());
        }
      });
    }
    redisTemplate.delete(userKey);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Long size = redisTemplate.opsForList().size(userKey(userId));
    return size != null && size > 0;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACCESS_INDEX, accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(REFRESH_INDEX, refreshToken));
  }

  @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2))
  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.getUserDto().id();
    String userKey = userKey(userId);
    String lockValue = redisLockProvider.acquireLock(userId.toString());

    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation info
              && info.getRefreshToken().equals(oldRefreshToken)) {
            removeIndex(info.getAccessToken(), info.getRefreshToken());
            removeAt(userKey, i);
            break;
          }
        }
      }
      redisTemplate.opsForList().rightPush(userKey, newJwtInformation);
      redisTemplate.expireAt(userKey, newJwtInformation.getExpiration());
      addIndex(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());
    } finally {
      redisLockProvider.releaseLock(userId.toString(), lockValue);
    }
    return newJwtInformation;
  }

  @Scheduled(fixedDelayString = "${discodeit.jwt.cleanup-interval}")
  @Override
  public void clearExpiredJwtInformation() {
    Set<String> userKeys = redisTemplate.keys(USER_KEY_PREFIX + "*");
    if (userKeys == null) {
      return;
    }
    for (String userKey : userKeys) {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
      if (tokens == null) {
        continue;
      }
      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i) instanceof JwtInformation info && info.isExpired()) {
          removeIndex(info.getAccessToken(), info.getRefreshToken());
          removeAt(userKey, i);
        }
      }
      Long size = redisTemplate.opsForList().size(userKey);
      if (size == null || size == 0) {
        redisTemplate.delete(userKey);
      }
    }
    log.debug("만료 JWT 정리 완료");
  }

  private void removeAt(String userKey, int index) {
    redisTemplate.opsForList().set(userKey, index, REMOVAL_MARKER);
    redisTemplate.opsForList().remove(userKey, 1, REMOVAL_MARKER);
  }

  private String userKey(UUID userId) {
    return USER_KEY_PREFIX + userId;
  }

  private void addIndex(String accessToken, String refreshToken) {
    redisTemplate.opsForSet().add(ACCESS_INDEX, accessToken);
    redisTemplate.opsForSet().add(REFRESH_INDEX, refreshToken);
  }

  private void removeIndex(String accessToken, String refreshToken) {
    redisTemplate.opsForSet().remove(ACCESS_INDEX, accessToken);
    redisTemplate.opsForSet().remove(REFRESH_INDEX, refreshToken);
  }
}