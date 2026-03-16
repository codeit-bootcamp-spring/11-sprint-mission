package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ReadStatusCreateRequestDto(
        UUID userId,
        UUID channelId
) {
}
