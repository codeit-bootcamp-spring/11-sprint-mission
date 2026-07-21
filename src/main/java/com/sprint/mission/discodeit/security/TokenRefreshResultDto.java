package com.sprint.mission.discodeit.security;

public record TokenRefreshResultDto(
    JwtDto jwtDto,
    String newRefreshToken
) {

}
