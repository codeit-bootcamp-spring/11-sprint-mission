package com.sprint.mission.discodeit.security.jwt.dto;

import com.sprint.mission.discodeit.dto.user.UserDto;

public record JwtSessionResult(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {

}
