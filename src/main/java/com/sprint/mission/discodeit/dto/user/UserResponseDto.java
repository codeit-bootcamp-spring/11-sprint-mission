package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String email,
        UUID profileId,
        Boolean online
) {
}
