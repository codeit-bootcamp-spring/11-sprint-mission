package com.sprint.mission.discodeit.dto.data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto (
    UUID id,
    Instant createdAt,
    Instant updateAt,
    String content,
    UUID channelId,
    UUID authorId,
    List<UUID> attachmentIds
 ) {
}
