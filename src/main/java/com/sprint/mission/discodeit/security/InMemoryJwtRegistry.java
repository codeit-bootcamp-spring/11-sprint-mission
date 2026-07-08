package com.sprint.mission.discodeit.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  private static final int MAX_ACTIVE_JWT_COUNT = 1;

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    // 사용자별 JWT 정보를 저장함
    Queue<JwtInformation> jwtInformations = origin.computeIfAbsent(
        jwtInformation.userId(),
        userId -> new ArrayDeque<>()
    );

    synchronized (jwtInformations) {
      jwtInformations.add(jwtInformation);

      // 최대 동시 로그인 개수를 초과하면 오래된 JWT 정보부터 제거함
      while (jwtInformations.size() > MAX_ACTIVE_JWT_COUNT) {
        jwtInformations.poll();
      }
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    // 특정 사용자의 모든 JWT 정보를 제거함
    origin.remove(userId);
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    // refresh token과 일치하는 JWT 정보를 제거함
    origin.values().forEach(jwtInformations -> {
      synchronized (jwtInformations) {
        jwtInformations.removeIf(jwtInformation ->
            jwtInformation.refreshToken().equals(refreshToken)
        );
      }
    });
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> jwtInformations = origin.get(userId);

    // 사용자 JWT 정보가 하나라도 있으면 로그인 중으로 판단함
    return jwtInformations != null && !jwtInformations.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    // access token이 registry에 남아있는지 확인함
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwtInformation -> jwtInformation.accessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    // refresh token이 registry에 남아있는지 확인함
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwtInformation -> jwtInformation.refreshToken().equals(refreshToken));
  }

  @Override
  public void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {
    // 기존 refresh token 정보를 제거하고 새 JWT 정보를 등록함
    invalidateJwtInformationByRefreshToken(oldRefreshToken);
    registerJwtInformation(newJwtInformation);
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();

    // 만료된 refresh token 정보를 주기적으로 제거함
    origin.values().forEach(jwtInformations -> {
      synchronized (jwtInformations) {
        Iterator<JwtInformation> iterator = jwtInformations.iterator();

        while (iterator.hasNext()) {
          JwtInformation jwtInformation = iterator.next();

          if (jwtInformation.refreshTokenExpiresAt().isBefore(now)) {
            iterator.remove();
          }
        }
      }
    });

    // 비어있는 사용자 entry 제거함
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}