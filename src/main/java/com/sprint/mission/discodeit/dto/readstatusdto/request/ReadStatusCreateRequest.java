package com.sprint.mission.discodeit.dto.readstatusdto.request;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(

    UUID userId,
    UUID channelId,
    Instant lastReadAt

) {

}
