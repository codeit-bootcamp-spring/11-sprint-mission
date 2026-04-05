package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusDto(UUID id, UUID userId, UUID channelId,
                            Instant createdAt, Instant updatedAt, Instant lastReadAt) {

}
