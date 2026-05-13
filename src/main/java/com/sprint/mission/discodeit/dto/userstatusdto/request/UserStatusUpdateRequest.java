package com.sprint.mission.discodeit.dto.userstatusdto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UserStatusUpdateRequest(
    @NotNull(message = "최근 활성화 시간은 null이 될 수 없습니다.")
    Instant newLastActiveAt
) {

}
