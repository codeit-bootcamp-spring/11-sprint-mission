package com.sprint.mission.discodeit.security.jwt;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryJwtRegistryTest {

    @Test
    void registerJwtInformation_limitsConcurrentLogin() {
        JwtRegistry jwtRegistry = new InMemoryJwtRegistry(1);
        UUID userId = UUID.randomUUID();

        jwtRegistry.registerJwtInformation(jwtInformation(
                userId,
                "old-access-token",
                "old-refresh-token"
        ));
        jwtRegistry.registerJwtInformation(jwtInformation(
                userId,
                "new-access-token",
                "new-refresh-token"
        ));

        assertThat(jwtRegistry.hasActiveJwtInformationByAccessToken(
                "old-access-token")).isFalse();
        assertThat(jwtRegistry.hasActiveJwtInformationByAccessToken(
                "new-access-token")).isTrue();
    }

    @Test
    void rotateJwtInformation_invalidatesOldTokens() {
        JwtRegistry jwtRegistry = new InMemoryJwtRegistry(1);
        UUID userId = UUID.randomUUID();
        jwtRegistry.registerJwtInformation(jwtInformation(
                userId,
                "old-access-token",
                "old-refresh-token"
        ));

        jwtRegistry.rotateJwtInformation(
                "old-refresh-token",
                jwtInformation(userId, "new-access-token", "new-refresh-token")
        );

        assertThat(jwtRegistry.hasActiveJwtInformationByRefreshToken(
                "old-refresh-token")).isFalse();
        assertThat(jwtRegistry.hasActiveJwtInformationByRefreshToken(
                "new-refresh-token")).isTrue();
    }

    @Test
    void clearExpiredJwtInformation_removesExpiredTokens() {
        JwtRegistry jwtRegistry = new InMemoryJwtRegistry(1);
        UUID userId = UUID.randomUUID();
        jwtRegistry.registerJwtInformation(new JwtInformation(
                userId,
                "access-token",
                "refresh-token",
                Instant.now().minusSeconds(1)
        ));

        jwtRegistry.clearExpiredJwtInformation();

        assertThat(jwtRegistry.hasActiveJwtInformationByUserId(userId)).isFalse();
    }

    private JwtInformation jwtInformation(
            UUID userId,
            String accessToken,
            String refreshToken
    ) {
        return new JwtInformation(
                userId,
                accessToken,
                refreshToken,
                Instant.now().plusSeconds(60)
        );
    }
}
