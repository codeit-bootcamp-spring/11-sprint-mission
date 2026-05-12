package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Message 읽음 상태 생성 정보")
public record ReadStatusCreateRequest(
        @NotNull(message = "사용자 ID는 필수로 기재해야 합니다.")
        UUID userId,

        @NotNull(message = "채널 ID는 필수로 기재해야 합니다.")
        UUID channelId,

        @NotNull(message = "마지막 읽음 시간은 필수로 기재해야 합니다.")
        Instant lastReadAt
) {
}
