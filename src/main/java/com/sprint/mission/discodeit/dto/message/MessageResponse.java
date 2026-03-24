package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;

import java.util.List;
import java.util.UUID;

public record MessageResponse(
        String content,
        UUID senderId,
        UUID channelId,
        List<BinaryContentResponse> attachments
) {}
