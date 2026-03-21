package com.sprint.mission.discodeit.dto;

import java.time.Instant;

public record UserStatusResponse(
        Instant lastLoginDate,
        boolean isOnline
) {}
