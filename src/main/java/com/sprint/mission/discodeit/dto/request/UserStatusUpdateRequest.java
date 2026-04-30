package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "변경할 User 온라인 상태 정보")
public record UserStatusUpdateRequest(

        @NotNull(message = "마지막 활성 시간은 필수로 기재해야 합니다.")
        Instant newLastActiveAt

) {
}
