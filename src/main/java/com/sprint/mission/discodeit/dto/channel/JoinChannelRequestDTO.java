package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinChannelRequestDTO(
        @NotNull(message = "요청 사용자 ID는 필수입니다.")
        UUID requestUserId,

        @NotNull(message = "대상 채널 ID는 필수입니다.")
        UUID channelId
) {
}
