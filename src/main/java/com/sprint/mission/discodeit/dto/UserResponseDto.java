package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String email,
        Boolean online
) {
}
