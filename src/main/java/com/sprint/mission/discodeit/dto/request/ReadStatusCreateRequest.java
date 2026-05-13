package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull(message = "유저ID는 공백일 수 없습니다.")
    UUID userId,

    @NotNull(message = "채널ID는 공백일 수 없습니다.")
    UUID channelId
) {

}
