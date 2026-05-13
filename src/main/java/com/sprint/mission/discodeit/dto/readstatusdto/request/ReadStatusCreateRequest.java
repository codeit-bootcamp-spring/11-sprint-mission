package com.sprint.mission.discodeit.dto.readstatusdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(

    @NotBlank(message = "유저 아이디는 필수 입력값입니다.")
    UUID userId,
    @NotBlank(message = "채널 아이디는 필수 입력값입니다.")
    UUID channelId,
    @NotNull(message = "최근 읽은 시간은 null이 될 수 없습니다.")
    Instant lastReadAt

) {

}
