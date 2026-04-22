package com.sprint.mission.discodeit.dto.readstatusdto.request;

import java.time.Instant;

public record ReadStatusUpdateRequest(
    Instant newLastReadAt

) {

}
