package com.sprint.mission.discodeit.dto;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        UUID userId,
        UUID channelId,
        String content,
        List<BinaryContentCreateRequest> attachments
) {
}