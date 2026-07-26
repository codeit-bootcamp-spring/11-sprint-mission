package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;

@Getter
public class JwtInformation {

  private final UserDto.Response userDto;
  private String accessToken;
  private String refreshToken;
  private Instant expiration;   // 리프레시 토큰 기준

  @JsonCreator
  public JwtInformation(
      @JsonProperty("userDto") UserDto.Response userDto,
      @JsonProperty("accessToken") String accessToken,
      @JsonProperty("refreshToken") String refreshToken,
      @JsonProperty("expiration") Instant expiration) {
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