package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import lombok.Getter;

@Getter
public class JwtInformation {

  private final UserDto.Response userDto;
  private String accessToken;
  private String refreshToken;
  private Instant expiration;   // 리프레시 토큰 기준

  public JwtInformation(UserDto.Response userDto, String accessToken, String refreshToken,
      Instant expiration) {
    this.userDto = userDto;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiration = expiration;
  }

  public void rotate(String accessToken, String refreshToken, Instant expiration) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiration = expiration;
  }

  public boolean isExpired() {
    return expiration.isBefore(Instant.now());
  }
}