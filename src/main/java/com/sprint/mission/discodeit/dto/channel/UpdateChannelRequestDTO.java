package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateChannelRequestDTO(
        @NotNull(message = "요청 사용자 ID는 필수입니다.")
        UUID requestUserId,

        @NotNull(message = "수정할 채널 ID는 필수입니다.")
        UUID channelId,

        @NotBlank(message = "채널 이름은 필수입니다.")
        @Size(max = 50, message = "채널 이름은 50자를 초과할 수 없습니다.")
        String name,

        @Size(max = 255, message = "채널 설명은 255자를 초과할 수 없습니다.")
        String description
) {
}