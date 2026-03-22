package com.sprint.mission.discodeit.dto;

import java.util.List;
import java.util.UUID;

public record MessageResponse(
        String content,
        UUID senderId,
        UUID channelId,
        List<BinaryContentResponse> attachments
) {}
