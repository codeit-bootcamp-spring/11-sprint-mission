package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class RedisJwtRegistry implements JwtRegistry {

  private static final String SESSION_KEY_PREFIX = "jwt:session:";
  private static final String ACCESS_INDEX_PREFIX = "jwt:access-index:";
  private static final String REFRESH_INDEX_PREFIX = "jwt:refresh-index:";
  private static final String USER_SESSIONS_PREFIX = "jwt:user-sessions:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final int maxActiveJwtCount;

  public RedisJwtRegistry(
      StringRedisTemplate redisTemplate,
      ObjectMapper objectMapper,
      @Value("${jwt.max-active-jwt-count:1}") int maxActiveJwtCount) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(UUID userId, JwtInformation jwtInformation) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    String sessionId = UUID.randomUUID().toString();

    // 최대 동시 로그인 수 초과 시 가장 오래된 세션부터 제거 (FIFO)
    Long currentCount = redisTemplate.opsForZSet().zCard(userSessionsKey);
    if (currentCount != null) {
      while (currentCount >= maxActiveJwtCount) {
        Set<String> oldest = redisTemplate.opsForZSet().range(userSessionsKey, 0, 0);
        if (oldest == null || oldest.isEmpty()) {
          break;
        }
        String oldestSessionId = oldest.iterator().next();
        removeSession(userId, oldestSessionId);
        log.debug("최대 동시 로그인 수 초과로 기존 토큰 무효화: userId={}, sessionId={}", userId,
            oldestSessionId);
        currentCount--;
      }
    }

    saveSession(userId, sessionId, jwtInformation);
    redisTemplate.opsForZSet().add(userSessionsKey, sessionId, System.currentTimeMillis());
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<String> sessionIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
    if (sessionIds != null) {
      sessionIds.forEach(sessionId -> removeSession(userId, sessionId));
    }
    redisTemplate.delete(userSessionsKey);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<String> sessionIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
    if (sessionIds == null || sessionIds.isEmpty()) {
      return false;
    }
    // 세션 키 존재 여부 = refreshToken 만료 여부 (TTL로 자동 판별)
    return sessionIds.stream()
        .anyMatch(sessionId ->
            Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey(userId, sessionId))));
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_INDEX_PREFIX + accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_INDEX_PREFIX + refreshToken));
  }

  @Override
  public void rotateJwtInformation(String refreshToken, String newAccessToken,
      String newRefreshToken, Date newAccessTokenExpiration, Date newRefreshTokenExpiration) {
    String ref = redisTemplate.opsForValue().get(REFRESH_INDEX_PREFIX + refreshToken);
    if (ref == null) {
      log.debug("rotate 대상 refreshToken을 찾을 수 없음");
      return;
    }
    String[] parts = ref.split(":", 2);
    UUID userId = UUID.fromString(parts[0]);
    String sessionId = parts[1];

    JwtInformation jwtInformation = readSession(userId, sessionId);
    if (jwtInformation == null) {
      return;
    }

    String oldAccessToken = jwtInformation.getAccessToken();
    jwtInformation.rotate(newAccessToken, newRefreshToken, newAccessTokenExpiration,
        newRefreshTokenExpiration);

    saveSession(userId, sessionId, jwtInformation);

    redisTemplate.delete(ACCESS_INDEX_PREFIX + oldAccessToken);
    redisTemplate.delete(REFRESH_INDEX_PREFIX + refreshToken);

    String newRef = userId + ":" + sessionId;
    setIndex(ACCESS_INDEX_PREFIX + newAccessToken, newRef, newAccessTokenExpiration);
    setIndex(REFRESH_INDEX_PREFIX + newRefreshToken, newRef, newRefreshTokenExpiration);
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    String ref = redisTemplate.opsForValue().get(REFRESH_INDEX_PREFIX + refreshToken);
    if (ref == null) {
      return;
    }
    String[] parts = ref.split(":", 2);
    UUID userId = UUID.fromString(parts[0]);
    String sessionId = parts[1];

    removeSession(userId, sessionId);
    redisTemplate.opsForZSet().remove(USER_SESSIONS_PREFIX + userId, sessionId);
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    // TTL로 실제 데이터는 자동 소멸됨. 여기서는 ZSET에 남은 만료 sessionId 흔적만 정리.
    Set<String> userSessionKeys = redisTemplate.keys(USER_SESSIONS_PREFIX + "*");
    if (userSessionKeys == null) {
      return;
    }
    for (String userSessionsKey : userSessionKeys) {
      UUID userId = UUID.fromString(userSessionsKey.substring(USER_SESSIONS_PREFIX.length()));
      Set<String> sessionIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
      if (sessionIds == null) {
        continue;
      }
      for (String sessionId : sessionIds) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(sessionKey(userId, sessionId)))) {
          redisTemplate.opsForZSet().remove(userSessionsKey, sessionId);
        }
      }
    }
    log.debug("만료된 JWT 세션 인덱스 정리 완료");
  }

  private void saveSession(UUID userId, String sessionId, JwtInformation jwtInformation) {
    try {
      String json = objectMapper.writeValueAsString(jwtInformation);
      String ref = userId + ":" + sessionId;

      setValue(sessionKey(userId, sessionId), json, jwtInformation.getRefreshTokenExpiration());
      setIndex(ACCESS_INDEX_PREFIX + jwtInformation.getAccessToken(), ref,
          jwtInformation.getAccessTokenExpiration());
      setIndex(REFRESH_INDEX_PREFIX + jwtInformation.getRefreshToken(), ref,
          jwtInformation.getRefreshTokenExpiration());
    } catch (Exception e) {
      log.error("JwtInformation 직렬화 실패: userId={}", userId, e);
      throw new IllegalStateException("Redis에 JWT 정보를 저장하지 못했습니다.", e);
    }
  }

  private JwtInformation readSession(UUID userId, String sessionId) {
    String json = redisTemplate.opsForValue().get(sessionKey(userId, sessionId));
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, JwtInformation.class);
    } catch (Exception e) {
      log.error("JwtInformation 역직렬화 실패: userId={}, sessionId={}", userId, sessionId, e);
      return null;
    }
  }

  private void removeSession(UUID userId, String sessionId) {
    JwtInformation jwtInformation = readSession(userId, sessionId);
    if (jwtInformation != null) {
      redisTemplate.delete(ACCESS_INDEX_PREFIX + jwtInformation.getAccessToken());
      redisTemplate.delete(REFRESH_INDEX_PREFIX + jwtInformation.getRefreshToken());
    }
    redisTemplate.delete(sessionKey(userId, sessionId));
  }

  private void setValue(String key, String value, Date expiration) {
    long ttlMillis = expiration.getTime() - System.currentTimeMillis();
    if (ttlMillis <= 0) {
      return;
    }
    redisTemplate.opsForValue().set(key, value, Duration.ofMillis(ttlMillis));
  }

  private void setIndex(String key, String value, Date expiration) {
    setValue(key, value, expiration);
  }

  private String sessionKey(UUID userId, String sessionId) {
    return SESSION_KEY_PREFIX + userId + ":" + sessionId;
  }
}