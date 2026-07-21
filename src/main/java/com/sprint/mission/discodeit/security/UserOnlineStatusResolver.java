package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOnlineStatusResolver {

    private final JwtRegistry jwtRegistry;

    public Set<UUID> getOnlineUserIds() {
        return jwtRegistry.getActiveUserIds();
    }

    public void invalidateTokens(UUID userId) {
        jwtRegistry.invalidateJwtInformationByUserId(userId);
    }
}
