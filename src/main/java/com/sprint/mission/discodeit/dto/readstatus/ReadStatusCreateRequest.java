package com.sprint.mission.discodeit.dto.readstatus;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID userId,

        @NotNull(message = "채널 ID는 필수입니다.")
        UUID channelId,

        // API 명세서에 포함되어 있으나, 보통 클라이언트가 안 보내면 백엔드에서 현재 시간으로 처리합니다.
        Instant lastReadAt
) {}