package com.sprint.mission.discodeit.dto.global;

import java.time.Instant;

public record ErrorResponse(
        Instant timeStamp,
        int status,
        String error,
        String message
) {
}
