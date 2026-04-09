package com.sprint.mission.discodeit.dto.message;

import java.util.UUID;

public record MessageResponse(
    UUID id,
    String content,
    UUID channelId,
    UUID authorId
) {

}
