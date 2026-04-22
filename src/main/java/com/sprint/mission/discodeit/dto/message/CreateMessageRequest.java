package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMessageRequest(
    @NotNull(message = "채널ID는 비어 있을 수 없습니다.")
    UUID channelId,

    @NotNull(message = "유저ID는 비어 있을 수 없습니다.")
    UUID authorId,

    @NotBlank(message = "메시지 내용은 비어 있을 수 없습니다.")
    String content
) {

}
