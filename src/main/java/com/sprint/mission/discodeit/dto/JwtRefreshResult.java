package com.sprint.mission.discodeit.dto;

public record JwtRefreshResult(
    String accessToken,
    String refreshToken,
    UserDto userDto
) {

}