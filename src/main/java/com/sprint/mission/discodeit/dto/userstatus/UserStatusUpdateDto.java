package com.sprint.mission.discodeit.dto.userstatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record UserStatusUpdateDto(
    @NotNull(message = "최근 활동 시간은 필수입니다.")
    @PastOrPresent(message = "최근 활동 시간은 현재 또는 과거의 시간이어야 합니다.")
    Instant newLastActiveAt
) {

}
