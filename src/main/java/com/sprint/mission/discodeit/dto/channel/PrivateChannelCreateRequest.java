package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Private Channel 생성 정보")
public record PrivateChannelCreateRequest(
        @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
        List<UUID> participantIds
) {
}
