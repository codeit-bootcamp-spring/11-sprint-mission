package com.sprint.mission.discodeit.dto.userdto;

import java.util.UUID;

public record DeleteUserDto(
        UUID userId,
        String password
) {
}
