package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtInformation {

  private final UserResponse userResponse;
  private String accessToken;
  private String refreshToken;
  private Instant expiration;

  public void rotate(String newAccessToken, String newRefreshToken, Instant newExpiration) {
    this.accessToken = newAccessToken;
    this.refreshToken = newRefreshToken;
    this.expiration = newExpiration;
  }
}