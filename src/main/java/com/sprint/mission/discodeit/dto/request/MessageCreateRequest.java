package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageCreateRequest(

        String content,

        @NotNull(message = "메시지가 업로드되는 채널은 필수로 기재해야 합니다.")
        UUID channelId,

        @NotNull(message = "메시지를 보내는 유저는 필수로 기재해야 합니다.")
        UUID authorId
) {
}
