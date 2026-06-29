package com.sprint.mission.discodeit.dto.response;

public record JwtDto(
    String accessToken,
    String tokenType
) {

  public static JwtDto of(String accessToken) {
    return new JwtDto(accessToken, "Bearer");
  }
}