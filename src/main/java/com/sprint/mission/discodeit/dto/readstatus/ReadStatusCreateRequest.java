package com.sprint.mission.discodeit.dto.readstatus;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull(message = "사용자 아이디는 필수입니다.")
    UUID userId,

    @NotNull(message = "채널 아이디는 필수입니다.")
    UUID channelId,

    Instant lastReadAt
) {

}