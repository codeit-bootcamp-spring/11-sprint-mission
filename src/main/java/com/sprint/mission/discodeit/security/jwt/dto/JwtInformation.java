package com.sprint.mission.discodeit.security.jwt.dto;

import java.time.Instant;
import java.util.UUID;

public record JwtInformation(
    UUID userId,
    String accessToken,
    String refreshToken,
    Instant expiration
) {

  public JwtInformation rotate(String newAccessToken, String newRefreshToken,
      Instant newExpiration) {
    return new JwtInformation(userId, newAccessToken, newRefreshToken, newExpiration);
  }
}
