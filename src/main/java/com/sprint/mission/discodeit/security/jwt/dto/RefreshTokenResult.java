package com.sprint.mission.discodeit.security.jwt.dto;

public record RefreshTokenResult(
    JwtDto jwtDto,
    String newRefreshToken
) {

}
