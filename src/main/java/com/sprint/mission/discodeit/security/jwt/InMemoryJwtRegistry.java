package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserOnlineStatusChangedEvent;
import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final Map<String, JwtInformation> accessTokenIndex = new ConcurrentHashMap<>();
  private final Map<String, JwtInformation> refreshTokenIndex = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;
  private final ApplicationEventPublisher eventPublisher;

  public InMemoryJwtRegistry(int maxActiveJwtCount, ApplicationEventPublisher eventPublisher) {
    this.maxActiveJwtCount = maxActiveJwtCount;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.userId();
    Queue<JwtInformation> queue = origin.computeIfAbsent(
        userId, id -> new ArrayDeque<>());

    boolean wasOffline;
    synchronized (queue) {
      wasOffline = queue.isEmpty();
      queue.add(jwtInformation);
      indexPut(jwtInformation);
      while (queue.size() > maxActiveJwtCount) {
        JwtInformation evicted = queue.poll();
        if (evicted != null) {
          indexRemove(evicted);
        }
      }
    }

    if (wasOffline) {
      eventPublisher.publishEvent(new UserOnlineStatusChangedEvent(userId, true));
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.remove(userId);
    if (queue != null) {
      boolean wasOnline;
      synchronized (queue) {
        wasOnline = !queue.isEmpty();
        queue.forEach(this::indexRemove);
      }
      if (wasOnline) {
        eventPublisher.publishEvent(new UserOnlineStatusChangedEvent(userId, false));
      }
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return accessTokenIndex.containsKey(accessToken);
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return refreshTokenIndex.containsKey(refreshToken);
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    Queue<JwtInformation> queue = origin.get(newJwtInformation.userId());
    if (queue != null) {
      synchronized (queue) {
        queue.removeIf(info -> {
          boolean match = info.refreshToken().equals(oldRefreshToken);
          if (match) {
            indexRemove(info);
          }
          return match;
        });
      }
    }

    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  public Set<UUID> getActiveUserIds() {
    return Set.copyOf(origin.keySet());
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    origin.forEach((userId, queue) -> {
      boolean hadEntries;
      boolean emptyAfter;
      synchronized (queue) {
        hadEntries = !queue.isEmpty();
        queue.removeIf(info -> {
          boolean expired = info.expiration().isBefore(now);
          if (expired) {
            indexRemove(info);
          }
          return expired;
        });
        emptyAfter = queue.isEmpty();
      }
      if (hadEntries && emptyAfter) {
        eventPublisher.publishEvent(new UserOnlineStatusChangedEvent(userId, false));
      }
    });
    origin.values().removeIf(Queue::isEmpty);
  }

  private void indexPut(JwtInformation info) {
    accessTokenIndex.put(info.accessToken(), info);
    refreshTokenIndex.put(info.refreshToken(), info);
  }

  private void indexRemove(JwtInformation info) {
    accessTokenIndex.remove(info.accessToken());
    refreshTokenIndex.remove(info.refreshToken());
  }

}
