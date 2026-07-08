package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.UUID;

public record JwtInformation(
    UUID userId,
    String accessToken,
    String refreshToken,
    Instant expiration
) {

}
