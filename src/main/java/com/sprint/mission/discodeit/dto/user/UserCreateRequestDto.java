package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserCreateRequestDto(
        String name,
        String email,
        String password,
        UUID profileId
) {
}
