package com.sprint.mission.discodeit.security.jwt;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;

@Getter
public class JwtInformation {

  private final UUID userId;
  private String accessToken;
  private Date accessTokenExpiration;
  private String refreshToken;
  private Date refreshTokenExpiration;

  public JwtInformation(UUID userId, String accessToken, String refreshToken,
      Date accessTokenExpiration, Date refreshTokenExpiration) {
    this.userId = userId;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public boolean isAccessTokenExpired() {
    return accessTokenExpiration.before(new Date());
  }

  public boolean isRefreshTokenExpired() {
    return refreshTokenExpiration.before(new Date());
  }

  public void rotate(String newAccessToken, String newRefreshToken,
      Date newAccessTokenExpiration, Date newRefreshTokenExpiration) {
    this.accessToken = newAccessToken;
    this.accessTokenExpiration = newAccessTokenExpiration;
    this.refreshToken = newRefreshToken;
    this.refreshTokenExpiration = newRefreshTokenExpiration;
  }
}