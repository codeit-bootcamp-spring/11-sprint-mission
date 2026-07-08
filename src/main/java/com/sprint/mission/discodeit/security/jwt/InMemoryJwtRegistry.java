package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.dto.jwt.JwtInformation;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;


public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;

  public InMemoryJwtRegistry(int maxActiveJwtCount,
      JwtTokenProvider jwtTokenProvider) {
    this.maxActiveJwtCount = maxActiveJwtCount;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserDto().id();

    //큐 가져오기
    Queue<JwtInformation> queue = origin.computeIfAbsent(
        userId, key -> new ConcurrentLinkedQueue<>());

    queue.add(jwtInformation);

    //동시 접속 가능에 맞게 오래된 순서부터 삭제
    while (queue.size() > maxActiveJwtCount) {
      queue.poll();
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
        .anyMatch(info -> info.getAccessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.getRefreshToken().equals(refreshToken));
  }

  @Override
  public JwtInformation rotateJwtInformation(String oldRefreshToken,
      JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.getUserDto().id();
    Queue<JwtInformation> queue = origin.get(userId);

    //기존 삭제
    if (queue != null) {
      queue.removeIf(info -> info.getRefreshToken().equals(oldRefreshToken));
    }

    //새거 만들기
    registerJwtInformation(newJwtInformation);
    return newJwtInformation;
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 5)   // 5분
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    //모든 큐를 돌면서 만료된거 제거
    origin.values().forEach(queue ->
        queue.removeIf(info ->
            jwtTokenProvider.getExpiration(info.getRefreshToken()).isBefore(now)));
    //빈 큐 제거
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());

  }
}
