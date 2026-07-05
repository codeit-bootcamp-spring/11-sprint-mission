package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import java.time.Instant;

public record JwtInformation(
    UserResponse userResponse,
    String accessToken,
    String refreshToken,
    Instant expiration
) {

  public JwtInformation rotate(String newAccessToken, String newRefreshToken,
      Instant newExpiration) {
    return new JwtInformation(userResponse, newAccessToken, newRefreshToken, newExpiration);
  }
}