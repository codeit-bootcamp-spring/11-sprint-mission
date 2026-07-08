package com.sprint.mission.discodeit.security.jwt.registry;

import com.sprint.mission.discodeit.security.jwt.model.JwtInformation;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;

public class InMemoryJwtRegistry implements JwtRegistry {

  // 전체 사용자 로그인 세션 목록
  // Key : UUID(userId), Value : Queue<JwtInformation>
  // Queue<JwtInformation>은 로그인 세션 목록이고 userId를 Key로 하는 Map으로 묶어서 특정 사용자의 로그인 세션 목록이 됨
  // 멀티스레드에서 일반 HashMap으로 put remove시 데이터 정합성이 깨짐
  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

  // 사용자가 가질 수 있는 최대 로그인 수 
  // 1일 경우 동시 로그인 제한
  private final int maxActiveJwtCount;

  public InMemoryJwtRegistry(int maxActiveJwtCount) {
    this.maxActiveJwtCount = maxActiveJwtCount;
  }


  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {

    // k는 jwtInformation.userId()
    // computeIfAbsent : Key(UUID)에 해당 userId가 없으면 userId Key도 같이 생성 & 비어있는 새 ConcurrentLinkedQueue가 Value
    // ConcurrentLinkedQueue에
    // ConcurrentLinkedQueue를 사용하는 이유
    // 멀티스레드에 적합한(동시성 문제) Concurrent 자료구조를 선택
    // 빠른 삽입/삭제를 위해 Linked 자료구조를 선택
    // 오래된걸 먼저 삭제하는 등 순서가 중요하기 때문에 Queue 자료구조를 선택
    Queue<JwtInformation> queue = origin.computeIfAbsent(jwtInformation.userId(),
        k -> new ConcurrentLinkedQueue<>());

    // offer: add, 데이터를 추가
    queue.offer(jwtInformation);

    // poll() : 가장 오래된(=먼저 들어온) 데이터를 제거
    // size가 maxActiveJwtCount보다 클 경우 maxActiveJwtCount가 될 때까지 반복해서 제거
    while (queue.size() > maxActiveJwtCount) {
      queue.poll();
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    // Key가 userId인 모든 요소들 제거(모든 userId 로그아웃)
    origin.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {

    // ConcurrentHashMap에서 userId 키에 해당하는 Value들을 가져옴
    Queue<JwtInformation> queue = origin.get(userId);

    // userId가 존재하면서 queue가 비어있지 않으면 true
    // origin에서 userId(Key)가 존재하지 않거나 userId는 있지만 userId에 해당하는 Value 값이 비어있으면 false
    return queue != null && !queue.isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {

    // Queue<JwtInformation>의 JwtInformation 조회 및 검사를 위해 stream을 사용
    // origin.values()는 Collection<Queue<JwtInformation>>
    // origin.values().stream()은 Stream<Queue<JwtInformation>>
    // 전체 사용자의 JwtInformation을 가져옴
    // stream()을 통해 흐름으로 바꿈 → 여러 Queue<JwtInformation>가 존재하고 하나씩 꺼냄
    // flatMap(Collection::stream)을 통해 Queue<JwtInformation>들을 펼침 → 여러 JwtInformation가 존재하고 하나씩 꺼냄
    // 찾고 있는 Access Token이 하나라도 origin에 있으면 true
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.accessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {

    // 전체 사용자의 JwtInformation을 가져옴
    // 찾고 있는 Refresh Token이 하나라도 origin에 있으면 true
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.refreshToken().equals(refreshToken));
  }

  @Override
  public JwtInformation rotateJwtInformation(
      String oldRefreshToken,
      JwtInformation newJwtInformation
  ) {

    // 오래된 토큰을 삭제해서 Collection 수정이 필요하기 때문에 forEach를 사용
    // removeIf : 조건에 맞는 요소를 삭제(true 반환 전 삭제 후 true를 반환하기 때문에 boolean 타입)
    // 만료된 Refresh Token을 먼저 삭제
    origin.values().forEach(queue ->
        queue.removeIf(info -> info.refreshToken().equals(oldRefreshToken))
    );

    // 새로운 Access Token, Refresh Token 발급
    registerJwtInformation(newJwtInformation);

    return newJwtInformation;
  }

  @Override
  @Scheduled(fixedDelay = 5 * 60 * 1000) // 5분
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();

    // userId가 Key, queue가 Value
    origin.forEach((userId, queue) -> {
      // 만료시간(expiration)이 지나면 삭제
      queue.removeIf(info -> info.expiration().isBefore(now));

      // 남아있는 토큰이 없다면(모두 만료되서 삭제됐을 때)
      // Key가 userId인 요소들 제거(Value인 queue도 전부 제거하여 메모리 절약)
      if (queue.isEmpty()) {
        origin.remove(userId);
      }
    });
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {

    // Refresh Token을 삭제하여 Collection 수정이 필요하기 때문에 forEach를 사용
    origin.values().forEach(queue ->
        queue.removeIf(info -> info.refreshToken().equals(refreshToken))
    );
  }
}
