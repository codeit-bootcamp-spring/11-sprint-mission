package com.sprint.mission.discodeit.dto.message;

import java.util.List;
import java.util.UUID;

public record MessageDto(
        UUID id,
        String contents,
        UUID userId,
        UUID channelId,
        List<UUID>attachmentIds
) {
}
