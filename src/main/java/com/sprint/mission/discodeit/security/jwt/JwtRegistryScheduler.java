package com.sprint.mission.discodeit.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRegistryScheduler {

    private final JwtRegistry jwtRegistry;

    @Scheduled(fixedRate = 3600000) // 1시간마다 만료된 토큰 정리
    public void clearExpiredJwtInformation() {
        jwtRegistry.clearExpiredJwtInformation();
    }
}
