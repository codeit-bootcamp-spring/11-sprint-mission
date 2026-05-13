package com.sprint.mission.discodeit.service.dto.readstatus;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateReadStatusRequest(
        @NotNull UUID userId,
        @NotNull UUID channelId,
        Instant lastReadAt
) {
}
