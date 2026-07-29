package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.JwtInformation;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.event.message.UserUpdatedEvent;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;


@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

  // <userId, Queue<JwtInformation>>
  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
  private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;
  private final ApplicationEventPublisher eventPublisher;

  @CacheEvict(value = "users", key = "'all'")
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UserDto before = withOnline(jwtInformation.getUserDto(), false);

    origin.compute(jwtInformation.getUserDto().id(), (key, queue) -> {
      if (queue == null) {
        queue = new ConcurrentLinkedQueue<>();
      }
      // If the queue exceeds the max size, remove the oldest token
      if (queue.size() >= maxActiveJwtCount) {
        JwtInformation deprecatedJwtInformation = queue.poll();// Remove the oldest token
        if (deprecatedJwtInformation != null) {
          removeTokenIndex(
              deprecatedJwtInformation.getAccessToken(),
              deprecatedJwtInformation.getRefreshToken()
          );
        }
      }
      queue.add(jwtInformation); // Add the new token
      addTokenIndex(
          jwtInformation.getAccessToken(),
          jwtInformation.getRefreshToken()
      );
      return queue;
    });

    UserDto after = withOnline(jwtInformation.getUserDto(), true);
    eventPublisher.publishEvent(
        new UserUpdatedEvent(before, after, Instant.now())
    );
  }

  @CacheEvict(value = "users", key = "'all'")
  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    UserDto userDto = origin.getOrDefault(userId, new ConcurrentLinkedQueue<>())
        .stream()
        .findFirst()
        .map(JwtInformation::getUserDto)
        .orElse(null);

    origin.computeIfPresent(userId, (key, queue) -> {
      queue.forEach(jwtInformation -> {
        removeTokenIndex(
            jwtInformation.getAccessToken(),
            jwtInformation.getRefreshToken()
        );
      });
      queue.clear(); // Clear the queue for this user
      return null; // Remove the user from the registry
    });

    if (userDto != null) {
      UserDto before = withOnline(userDto, true);
      UserDto after = withOnline(userDto, false);

      eventPublisher.publishEvent(
          new UserUpdatedEvent(before, after, Instant.now())
      );
    }
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
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    origin.computeIfPresent(newJwtInformation.getUserDto().id(), (key, queue) -> {
      queue.stream().filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
          .findFirst()
          .ifPresent(jwtInformation -> {
            removeTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
            jwtInformation.rotate(
                newJwtInformation.getAccessToken(),
                newJwtInformation.getRefreshToken()
            );
            addTokenIndex(
                newJwtInformation.getAccessToken(),
                newJwtInformation.getRefreshToken()
            );
          });
      return queue;
    });
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.entrySet().removeIf(entry -> {
      Queue<JwtInformation> queue = entry.getValue();
      UserDto userDto = queue.stream()
          .findFirst()
          .map(JwtInformation::getUserDto)
          .orElse(null);

      queue.removeIf(jwtInformation -> {
        boolean isExpired =
            !jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken()) ||
                !jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
        if (isExpired) {
          removeTokenIndex(
              jwtInformation.getAccessToken(),
              jwtInformation.getRefreshToken()
          );
        }
        return isExpired;
      });

      boolean empty = queue.isEmpty();

      if (empty && userDto != null) {
        eventPublisher.publishEvent(
            new UserUpdatedEvent(
                withOnline(userDto, true),
                withOnline(userDto, false),
                Instant.now()
            )
        );
      }

      return empty; // Remove the entry if the queue is empty
    });
  }

  private void addTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.add(accessToken);
    refreshTokenIndexes.add(refreshToken);
  }

  private void removeTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.remove(accessToken);
    refreshTokenIndexes.remove(refreshToken);
  }

  private UserDto withOnline(UserDto userDto, Boolean online) {
    return new UserDto(
        userDto.id(),
        userDto.username(),
        userDto.email(),
        userDto.profile(),
        online,
        userDto.role()
    );
  }
}