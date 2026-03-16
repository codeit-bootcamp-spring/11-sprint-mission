package com.sprint.mission.discodeit.dto.message;

import java.util.List;
import java.util.UUID;

public record MessageUpdateRequestDto(
        UUID messageId,
        String contents,
        List<UUID> attachmentIds
) {
}
