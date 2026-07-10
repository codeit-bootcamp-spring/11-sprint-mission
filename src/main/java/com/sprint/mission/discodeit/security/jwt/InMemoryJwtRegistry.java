package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;

public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Queue<JwtInformation> queue = origin.computeIfAbsent(
        jwtInformation.userId(),
        k -> new ConcurrentLinkedQueue<>()
    );

    queue.add(jwtInformation);

    while (queue.size() > maxActiveJwtCount) {
      queue.poll();
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      return;
    }
    origin.values().forEach(queue ->
        queue.removeIf(info -> info.refreshToken().equals(refreshToken))
    );
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);

    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.accessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.refreshToken().equals(refreshToken));
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.userId();
    Queue<JwtInformation> queue = origin.get(userId);

    if (queue != null) {
      queue.removeIf(info -> info.refreshToken().equals(oldRefreshToken));
    }

    registerJwtInformation(newJwtInformation);

    return null;
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();

    origin.values().forEach(queue ->
        queue.removeIf(info -> info.expiration().isBefore(now)));

    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

}
