package com.sprint.mission.discodeit.dto;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequestDto(
        String contents,
        UUID userId,
        UUID channelId,
        List<UUID> attachmentIds
) {
}
