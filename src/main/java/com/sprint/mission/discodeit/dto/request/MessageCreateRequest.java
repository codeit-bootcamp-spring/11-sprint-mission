package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MessageCreateRequest(
    @Size(max = 1000, message = "메시지 길이는 1000자를 넘을 수 없습니다.")
    String content,

    @NotNull(message = "채널 ID는 필수입니다.")
    UUID channelId,

    @NotNull(message = "작성자 ID는 필수입니다.")
    UUID authorId
) {

}
// UUID 타입은 NotBlank가 아닌 NotNull