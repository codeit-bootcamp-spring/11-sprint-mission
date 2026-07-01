package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Queue<JwtInformation> queue = origin.computeIfAbsent(
        jwtInformation.userId(), id -> new ArrayDeque<>());

    synchronized (queue) {
      queue.add(jwtInformation);
      while (queue.size() > maxActiveJwtCount) {
        queue.poll();
      }
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.accessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.refreshToken().equals(refreshToken));
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    Queue<JwtInformation> queue = origin.get(newJwtInformation.userId());
    if (queue != null) {
      synchronized (queue) {
        queue.removeIf(info -> info.refreshToken().equals(oldRefreshToken));
      }
    }

    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    origin.values().forEach(queue -> {
      synchronized (queue) {
        queue.removeIf(info -> info.expiration().isBefore(now));
      }
    });
    origin.values().removeIf(Queue::isEmpty);
  }

}
