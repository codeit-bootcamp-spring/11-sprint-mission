package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> userTokenRegistry = new ConcurrentHashMap<>();

  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(@Value("${discodeit.jwt.max-active-count}") int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  @CacheEvict(cacheNames = "users", allEntries = true)
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Queue<JwtInformation> userTokens = userTokenRegistry.computeIfAbsent(
        jwtInformation.userId(),
        k -> new ConcurrentLinkedQueue<>()
    );
    userTokens.add(jwtInformation);

    while (userTokens.size() > maxActiveJwtCount) {
      userTokens.poll();
    }
  }

  @Override
  @CacheEvict(cacheNames = "users", allEntries = true)
  public void invalidateJwtInformationByUserId(UUID userId) {
    userTokenRegistry.remove(userId);
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    userTokenRegistry.values()
        .forEach(queue -> queue.removeIf(jwt -> jwt.refreshToken().equals(refreshToken)));
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> userTokens = userTokenRegistry.get(userId);
    return userTokens != null && !userTokens.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return userTokenRegistry.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwt -> jwt.accessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return userTokenRegistry.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwt -> jwt.refreshToken().equals(refreshToken));
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    Queue<JwtInformation> userTokens = userTokenRegistry.get(newJwtInformation.userId());
    if (userTokens != null) {
      userTokens.removeIf(jwt -> jwt.refreshToken().equals(oldRefreshToken));
    }
    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();

    userTokenRegistry.forEach((userId, userTokens) -> {
      userTokens.removeIf(jwt -> jwt.expiration().isBefore(now));

      if (userTokens.isEmpty()) {
        userTokenRegistry.remove(userId);
      }
    });
  }
}
