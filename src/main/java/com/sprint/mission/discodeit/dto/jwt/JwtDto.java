package com.sprint.mission.discodeit.dto.jwt;

import com.sprint.mission.discodeit.dto.userdto.UserDto;

public record JwtDto(

    UserDto userDto,
    String accessToken

) {

}
