package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record MessageUpdate(
        UUID id,
        String message
) {
}
