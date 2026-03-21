package com.sprint.mission.discodeit.dto.message;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequestDto(
        String contents,
        UUID channelId,
        UUID userId,
        List<UUID> attachmentIds
) {
}
