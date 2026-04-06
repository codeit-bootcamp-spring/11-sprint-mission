package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record MessageCreatePart(
        UUID authorId,
        UUID channelId,
        String content
) {
}
