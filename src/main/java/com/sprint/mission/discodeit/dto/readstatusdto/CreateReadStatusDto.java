package com.sprint.mission.discodeit.dto.readstatusdto;

import java.time.Instant;
import java.util.UUID;

public record CreateReadStatusDto(

    UUID userId,
    UUID channelId,
    Instant lastReadAt

) {

}
