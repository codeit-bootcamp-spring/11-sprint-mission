package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReadStatusUpdateRequest(
    @NotNull(message = "lastReadAt은 필수입니다.")
    Instant lastReadAt
) {

}
