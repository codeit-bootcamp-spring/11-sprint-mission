package com.sprint.mission.discodeit.dto.data;

public record TokenPair(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {

}
