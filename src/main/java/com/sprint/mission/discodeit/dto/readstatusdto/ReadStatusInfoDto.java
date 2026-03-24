package com.sprint.mission.discodeit.dto.readstatusdto;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusInfoDto(

        UUID userId,
        UUID channelId,
        Instant updatedAt

) {
}
