package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull(message = "사용자 ID를 입력해주세요.")
    UUID userId,

    @NotNull(message = "채널 ID를 입력해주세요.")
    UUID channelId,

    @NotNull(message = "마지막 읽은 시간을 입력해주세요.")
    Instant lastReadAt
) {

}