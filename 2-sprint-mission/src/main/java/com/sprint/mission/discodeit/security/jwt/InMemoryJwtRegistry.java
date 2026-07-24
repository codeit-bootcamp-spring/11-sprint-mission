package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final Map<String, UUID> accessTokenIndexes = new ConcurrentHashMap<>();
  private final Map<String, UUID> refreshTokenIndexes = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserDto().id();

    origin.compute(userId, (key, queue) -> {
      Queue<JwtInformation> targetQueue = (queue == null) ? new ConcurrentLinkedQueue<>() : queue;

      accessTokenIndexes.put(jwtInformation.getAccessToken(), userId);
      refreshTokenIndexes.put(jwtInformation.getRefreshToken(), userId);

      targetQueue.offer(jwtInformation);

      while (targetQueue.size() > maxActiveJwtCount) {
        JwtInformation evicted = targetQueue.poll();
        if (evicted != null) {
          accessTokenIndexes.remove(evicted.getAccessToken());
          refreshTokenIndexes.remove(evicted.getRefreshToken());
        }
      }
      return targetQueue;
    });
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.compute(userId, (key, queue) -> {
      if (queue != null) {
        queue.forEach(info -> {
          accessTokenIndexes.remove(info.getAccessToken());
          refreshTokenIndexes.remove(info.getRefreshToken());
        });
      }
      return null;
    });
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return accessTokenIndexes.containsKey(accessToken);
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return refreshTokenIndexes.containsKey(refreshToken);
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = refreshTokenIndexes.get(oldRefreshToken);
    if (userId != null) {
      origin.compute(userId, (key, queue) -> {
        if (queue != null) {
          queue.removeIf(info -> {
            if (info.getRefreshToken().equals(oldRefreshToken)) {
              accessTokenIndexes.remove(info.getAccessToken());
              refreshTokenIndexes.remove(info.getRefreshToken());
              return true;
            }
            return false;
          });
        }
        return queue;
      });
    }

    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Scheduled(fixedDelayString = "${discodeit.jwt.cleanup-interval}")
  @Override
  public void clearExpiredJwtInformation() {
    origin.keySet().forEach(userId ->
        origin.computeIfPresent(userId, (key, queue) -> {
          queue.removeIf(info -> {
            if (info.isExpired()) {
              accessTokenIndexes.remove(info.getAccessToken());
              refreshTokenIndexes.remove(info.getRefreshToken());
              return true;
            }
            return false;
          });
          return queue.isEmpty() ? null : queue;
        })
    );
    log.debug("만료 JWT 정리 완료");
  }
}