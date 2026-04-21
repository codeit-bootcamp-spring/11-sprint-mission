package com.sprint.mission.discodeit.service.dto.readstatus;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ReadStatusDto(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant lastReadAt
) {
}
