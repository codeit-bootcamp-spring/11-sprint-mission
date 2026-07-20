package com.sprint.mission.discodeit.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRegistryScheduler {

    private final JwtRegistry jwtRegistry;

    @Scheduled(fixedDelay = 300000)
    public void clearExpiredJwtInformation() {
        jwtRegistry.clearExpiredJwtInformation();
    }
}
