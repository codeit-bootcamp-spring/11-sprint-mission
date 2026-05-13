package com.sprint.mission.discodeit.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReadStatusUpdateApiRequest(
        @NotNull Instant newLastReadAt
) {
}
