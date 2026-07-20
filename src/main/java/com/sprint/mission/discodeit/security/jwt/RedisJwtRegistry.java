package com.sprint.mission.discodeit.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "discodeit.jwt", name = "registry", havingValue = "redis")
public class RedisJwtRegistry implements JwtRegistry {

  private static final String REFRESH_KEY_PREFIX = "jwt:refresh:";
  private static final String USER_KEY_PREFIX = "jwt:user:";

  private final StringRedisTemplate redisTemplate;

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Duration ttl = Duration.between(Instant.now(), jwtInformation.expiration());
    if (ttl.isNegative() || ttl.isZero()) {
      return;
    }

    UUID userId = jwtInformation.userId();
    String refreshToken = jwtInformation.refreshToken();
    
    String previousRefreshToken = redisTemplate.opsForValue().get(userKey(userId));
    if (previousRefreshToken != null) {
      redisTemplate.delete(refreshKey(previousRefreshToken));
    }

    redisTemplate.opsForValue().set(refreshKey(refreshToken), userId.toString(), ttl);
    redisTemplate.opsForValue().set(userKey(userId), refreshToken, ttl);
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String refreshToken = redisTemplate.opsForValue().get(userKey(userId));
    if (refreshToken != null) {
      redisTemplate.delete(refreshKey(refreshToken));
    }
    redisTemplate.delete(userKey(userId));
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(userKey(userId)));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey(refreshToken)));
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    redisTemplate.delete(refreshKey(oldRefreshToken));
    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  public void clearExpiredJwtInformation() {
  }

  private String refreshKey(String refreshToken) {
    return REFRESH_KEY_PREFIX + refreshToken;
  }

  private String userKey(UUID userId) {
    return USER_KEY_PREFIX + userId;
  }
}