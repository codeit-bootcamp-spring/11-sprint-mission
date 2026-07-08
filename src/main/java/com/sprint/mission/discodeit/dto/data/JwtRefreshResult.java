package com.sprint.mission.discodeit.dto.data;

public record JwtRefreshResult(
    JwtDto jwtDto,
    String refreshToken
) {

}