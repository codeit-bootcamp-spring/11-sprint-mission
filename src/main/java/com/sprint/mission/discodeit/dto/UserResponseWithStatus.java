package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;

public record UserResponseWithStatus(
        UserDto user,
        UserStatus status
) {
}
