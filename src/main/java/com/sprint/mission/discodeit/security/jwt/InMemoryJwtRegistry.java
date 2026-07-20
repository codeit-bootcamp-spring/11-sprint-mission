package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryJwtRegistry implements JwtRegistry {
  
  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  // refreshToken -> userId 역인덱스로 조회를 O(1)로 처리한다. origin과 항상 함께 갱신한다.
  private final Map<String, UUID> refreshTokenIndex = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Queue<JwtInformation> queue = origin.computeIfAbsent(
        jwtInformation.userId(), key -> new ConcurrentLinkedQueue<>());
    queue.add(jwtInformation);
    refreshTokenIndex.put(jwtInformation.refreshToken(), jwtInformation.userId());

    while (queue.size() > maxActiveJwtCount) {
      JwtInformation evicted = queue.poll();
      if (evicted != null) {
        refreshTokenIndex.remove(evicted.refreshToken());
      }
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.remove(userId);
    if (queue != null) {
      queue.forEach(info -> refreshTokenIndex.remove(info.refreshToken()));
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return refreshTokenIndex.containsKey(refreshToken);
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = refreshTokenIndex.get(oldRefreshToken);
    if (userId != null) {
      Queue<JwtInformation> queue = origin.get(userId);
      if (queue != null) {
        queue.removeIf(info -> {
          boolean matched = info.refreshToken().equals(oldRefreshToken);
          if (matched) {
            refreshTokenIndex.remove(info.refreshToken());
          }
          return matched;
        });
      }
    }
    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    origin.values().forEach(queue -> queue.removeIf(info -> {
      boolean expired = info.expiration().isBefore(now);
      if (expired) {
        refreshTokenIndex.remove(info.refreshToken());
      }
      return expired;
    }));
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}