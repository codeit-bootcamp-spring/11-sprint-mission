package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(

        @NotBlank(message = "빈 메시지는 보낼 수 없습니다.")
        @Size(max = 300, message = "메시지는 300자 이하로 작성해야 합니다.")
        String contents,

        @NotNull(message = "메시지가 업로드되는 채널은 필수로 기재해야 합니다.")
        UUID channelId,

        @NotNull(message = "메시지를 보내는 유저는 필수로 기재해야 합니다.")
        UUID userId,

        List<UUID> attachmentIds
) {
}
