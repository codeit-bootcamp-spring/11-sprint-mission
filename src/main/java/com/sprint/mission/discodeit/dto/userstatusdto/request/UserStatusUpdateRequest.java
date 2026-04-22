package com.sprint.mission.discodeit.dto.userstatusdto.request;

import java.time.Instant;

public record UserStatusUpdateRequest(
    Instant newLastActiveAt
) {

}
