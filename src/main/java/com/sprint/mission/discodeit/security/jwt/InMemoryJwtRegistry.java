package com.sprint.mission.discodeit.security.jwt;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  // <userId, Queue<JwtInformation>>
  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(
      @Value("${jwt.max-active-jwt-count:1}") int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(UUID userId, JwtInformation jwtInformation) {
    Queue<JwtInformation> queue = origin.computeIfAbsent(userId,
        k -> new ConcurrentLinkedQueue<>());

    // 최대 동시 로그인 수 초과 시 오래된 토큰 제거
    while (queue.size() >= maxActiveJwtCount) {
      JwtInformation old = queue.poll();
      log.debug("최대 동시 로그인 수 초과로 기존 토큰 무효화: userId={}", userId);
    }

    queue.add(jwtInformation);
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    if (queue == null || queue.isEmpty()) {
      return false;
    }
    return queue.stream().anyMatch(info -> !info.isRefreshTokenExpired());
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.getAccessToken().equals(accessToken)
            && !info.isAccessTokenExpired());
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.getRefreshToken().equals(refreshToken)
            && !info.isRefreshTokenExpired());
  }

  @Override
  public void rotateJwtInformation(String refreshToken, String newAccessToken,
      String newRefreshToken, Date newAccessTokenExpiration, Date newRefreshTokenExpiration) {
    origin.values().stream()
        .flatMap(Queue::stream)
        .filter(info -> info.getRefreshToken().equals(refreshToken))
        .findFirst()
        .ifPresent(info -> info.rotate(
            newAccessToken, newRefreshToken, newAccessTokenExpiration, newRefreshTokenExpiration));
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.forEach((userId, queue) -> {
      queue.removeIf(JwtInformation::isRefreshTokenExpired);
      if (queue.isEmpty()) {
        origin.remove(userId);
      }
    });
    log.debug("만료된 JWT 정보 정리 완료");
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    origin.forEach((userId, queue) -> {
      boolean removed = queue.removeIf(
          info -> info.getRefreshToken().equals(refreshToken));
      if (removed && queue.isEmpty()) {
        origin.remove(userId);
      }
    });
  }
}