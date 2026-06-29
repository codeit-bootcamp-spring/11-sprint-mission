package com.sprint.mission.discodeit.dto.response;

public record JwtDto(
    String accessToken,
    String tokenType,
    UserDto userDto
) {

  public static JwtDto of(String accessToken, UserDto userDto) {
    return new JwtDto(accessToken, "Bearer", userDto);
  }
}