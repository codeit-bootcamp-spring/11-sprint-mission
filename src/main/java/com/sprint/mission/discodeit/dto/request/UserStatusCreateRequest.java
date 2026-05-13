package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record UserStatusCreateRequest(
    @NotNull(message = "유저ID는 필수여야 합니다.")
    UUID userId,

    @NotNull(message = "마지막으로 활동한 시간은 필수입니다.")
    Instant lastActiveAt
) {

}
