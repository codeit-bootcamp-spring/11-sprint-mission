package com.sprint.mission.discodeit.security.jwt.registry;

import com.sprint.mission.discodeit.event.user.UserLogInOutEvent;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import com.sprint.mission.discodeit.security.jwt.model.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisLockProvider redisLockProvider;

  @Override
  @CacheEvict(value = "users", key = "'all'")
  @Retryable(
      retryFor = RedisLockAcquisitionException.class,
      maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2))
  public void registerJwtInformation(JwtInformation jwtInformation) {
    String userKey = getUserKey(jwtInformation.userId());
    String lockKey = jwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);

    try {
      Long currentSize = redisTemplate.opsForList().size(userKey);

      while (currentSize != null && currentSize >= maxActiveJwtCount) {
        Object oldestTokenObj = redisTemplate.opsForList().leftPop(userKey);

        if (oldestTokenObj instanceof JwtInformation oldestToken) {
          removeTokenIndex(oldestToken.getAccessToken(), oldestToken.getRefreshToken());
        }

        currentSize = redisTemplate.opsForList().size(userKey);
      }

      redisTemplate.opsForList().rightPush(userKey, jwtInformation);
      redisTemplate.expire(userKey, DEFAULT_TTL);
      addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }

    eventPublisher.publishEvent(
        new UserLogInOutEvent(jwtInformation.userId(), true)
    );
  }

  @Override
  @CacheEvict(value = "users", key = "'all'")
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

    eventPublisher.publishEvent(
        new UserLogInOutEvent(userId, false)
    );
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    String userKey = getUserKey(userId);
    Long size = redisTemplate.opsForList().size(userKey);

    return size != null && size > 0;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    boolean result = Boolean.TRUE.equals(
        redisTemplate.opsForSet().isMember(ACCESS_TOKEN_INDEX_KEY, accessToken)
    );

    return result;
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(
        redisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken)
    );
  }

  @Override
  @Retryable(
      retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
      backoff = @Backoff(delay = 100, multiplier = 2)
  )
  public JwtInformation rotateJwtInformation(String refreshToken,
      JwtInformation newJwtInformation) {

    log.info("ROTATE ENTER userId={} oldRefresh={} newAccess={} newRefresh={}",
        newJwtInformation.userId(),
        refreshToken.substring(0, 20),
        newJwtInformation.getAccessToken().substring(0, 20),
        newJwtInformation.getRefreshToken().substring(0, 20)
    );

    String userKey = getUserKey(newJwtInformation.userId());
    String lockKey = newJwtInformation.userId().toString();

    redisLockProvider.acquireLock(lockKey);

    try {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

      log.info("ROTATE TOKENS SIZE={} userKey={}",
          tokens == null ? 0 : tokens.size(),
          userKey);

      if (tokens != null) {
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i) instanceof JwtInformation jwtInfo &&
              jwtInfo.getRefreshToken().equals(refreshToken)) {

            removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());

            jwtInfo.rotate(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());
            redisTemplate.opsForList().set(userKey, i, jwtInfo);
            addTokenIndex(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());
            redisTemplate.expire(userKey, DEFAULT_TTL);

            return jwtInfo;
          }
        }
      }
    } finally {
      redisLockProvider.releaseLock(lockKey);
    }
    return null;
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 5)
  public void clearExpiredJwtInformation() {
    Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");

    for (String userKey : userKeys) {
      List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

      if (tokens != null) {
        boolean hasValidTokens = false;

        for (int i = tokens.size() - 1; i >= 0; i--) {
          if (tokens.get(i) instanceof JwtInformation jwtInfo) {
            boolean isExpired = !jwtTokenProvider.validateToken(jwtInfo.getAccessToken()) ||
                !jwtTokenProvider.validateToken(jwtInfo.getRefreshToken());

            if (isExpired) {
              redisTemplate.opsForList().set(userKey, i, "EXPIRED");
              redisTemplate.opsForList().remove(userKey, 1, "EXPIRED");
              removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
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

  // 로그아웃 핸들러에서 이 메서드를 통해 로그아웃 되기 때문에 로그아웃 이벤트 발행은 이 메서드에서 호출
  // invalidateJwtInformationByUserId 메서드는 동시 로그인 제한을 위해 이전 로그인을 무효화시킴
  // 무효화 직후 바로 새 로그인에서 이벤트 발행되기 때문에 발행 메서드를 이쪽으로 옮김
  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");

    if (userKeys != null) {
      for (String userKey : userKeys) {

        List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

        if (tokens != null) {
          for (Object tokenObj : tokens) {

            if (tokenObj instanceof JwtInformation jwtInfo &&
                jwtInfo.getRefreshToken().equals(refreshToken)) {

              redisTemplate.opsForList().remove(userKey, 1, jwtInfo);

              removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());

              eventPublisher.publishEvent(
                  new UserLogInOutEvent(jwtInfo.userId(), false)
              );

              return;
            }
          }
        }
      }
    }
  }

  private String getUserKey(UUID userId) {
    return USER_JWT_KEY_PREFIX + userId.toString();
  }

  private void addTokenIndex(String accessToken, String refreshToken) {
    // Set에 토큰 추가 (add : 중복 시 무시됨)
    redisTemplate.opsForSet().add(ACCESS_TOKEN_INDEX_KEY, accessToken);
    redisTemplate.opsForSet().add(REFRESH_TOKEN_INDEX_KEY, refreshToken);

    // 인덱스 키에도 만료 시간 설정 (메모리 누수 방지)
    redisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, DEFAULT_TTL);
    redisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, DEFAULT_TTL);
  }

  private void removeTokenIndex(String accessToken, String refreshToken) {

    // Set에 토큰 제거
    redisTemplate.opsForSet().remove(ACCESS_TOKEN_INDEX_KEY, accessToken);
    redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);
  }
}
