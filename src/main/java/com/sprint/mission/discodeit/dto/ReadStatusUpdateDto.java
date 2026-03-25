package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ReadStatusUpdateDto(
        UUID userId,
        UUID channelId
) {
}
