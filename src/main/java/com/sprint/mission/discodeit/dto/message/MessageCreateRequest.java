package com.sprint.mission.discodeit.dto.message;

import java.util.UUID;

public record MessageCreateRequest(
        String content,
        UUID senderId,
        UUID channelId
) {}

