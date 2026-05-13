package com.sprint.mission.discodeit.service.dto.userstatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateUserStatusRequest(
        UUID userStatusId,
        Instant lastActiveAt
) {
}
