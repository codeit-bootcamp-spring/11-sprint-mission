package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;

public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
  private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    origin.compute(jwtInformation.getUserResponse().id(), (key, queue) -> {
      if (queue == null) {
        queue = new ConcurrentLinkedQueue<>();
      }
      if (queue.size() >= maxActiveJwtCount) {
        JwtInformation deprecated = queue.poll();
        if (deprecated != null) {
          removeTokenIndex(deprecated.getAccessToken(), deprecated.getRefreshToken());
        }
      }
      queue.add(jwtInformation);
      addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
      return queue;
    });
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.computeIfPresent(userId, (key, queue) -> {
      queue.forEach(info -> removeTokenIndex(info.getAccessToken(), info.getRefreshToken()));
      return null;
    });
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return origin.containsKey(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return accessTokenIndexes.contains(accessToken);
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return refreshTokenIndexes.contains(refreshToken);
  }

  @Override
  public void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {
    origin.computeIfPresent(newJwtInformation.getUserResponse().id(), (key, queue) -> {
      queue.stream()
          .filter(info -> info.getRefreshToken().equals(oldRefreshToken))
          .findFirst()
          .ifPresent(old -> {
            removeTokenIndex(old.getAccessToken(), old.getRefreshToken());
            old.rotate(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken(),
                newJwtInformation.getExpiration());
            addTokenIndex(old.getAccessToken(), old.getRefreshToken());
          });
      return queue;
    });
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    origin.keySet().forEach(userId ->
        origin.computeIfPresent(userId, (key, queue) -> {
          queue.removeIf(info -> {
            if (info.getExpiration().isBefore(now)) {
              removeTokenIndex(info.getAccessToken(), info.getRefreshToken());
              return true;
            }
            return false;
          });
          return queue.isEmpty() ? null : queue;
        })
    );
  }

  private void addTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.add(accessToken);
    refreshTokenIndexes.add(refreshToken);
  }

  private void removeTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.remove(accessToken);
    refreshTokenIndexes.remove(refreshToken);
  }
}