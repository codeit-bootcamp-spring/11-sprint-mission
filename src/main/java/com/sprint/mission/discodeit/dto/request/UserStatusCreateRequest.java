package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UserStatusCreateRequest(

        @NotNull(message = "사용자 ID는 필수로 기재해야 합니다.")
        UUID userId,

        @NotNull(message = "마지막 활성 시간은 필수로 기재해야 합니다.")
        Instant lastActiveAt

) {
}
