package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record LoginResponseDto(
        UUID id,
        String name,
        String email
) {
}
