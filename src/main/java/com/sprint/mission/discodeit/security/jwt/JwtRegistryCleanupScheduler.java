package com.sprint.mission.discodeit.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRegistryCleanupScheduler {

  private final JwtRegistry jwtRegistry;

  // 5분마다 만료된 토큰 정보를 정리한다. (이전 실행 종료 기준 fixedDelay)
  @Scheduled(fixedDelay = 1000 * 60 * 5)
  public void clearExpiredJwtInformation() {
    jwtRegistry.clearExpiredJwtInformation();
  }
}
