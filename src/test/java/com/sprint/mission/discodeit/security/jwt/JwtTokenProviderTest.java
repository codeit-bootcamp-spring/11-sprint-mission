package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.authority.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-jwt-secret-key-must-be-at-least-32-bytes",
            60_000,
            120_000
    );

    @Test
    void generateAndValidateToken_success() {
        UUID userId = UUID.randomUUID();
        DiscodeitUserDetails userDetails = new DiscodeitUserDetails(
                new UserDto(
                        userId,
                        "evan",
                        "evan@test.com",
                        null,
                        false,
                        UserRole.USER
                ),
                "password"
        );

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getSubject(accessToken))
                .isEqualTo(userId.toString());
        assertThat(accessToken).isNotEqualTo(refreshToken);
        assertThat(jwtTokenProvider.reissueAccessToken(refreshToken))
                .isNotEqualTo(accessToken);
    }

    @Test
    void validateToken_fail_whenTokenIsInvalid() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }
}
