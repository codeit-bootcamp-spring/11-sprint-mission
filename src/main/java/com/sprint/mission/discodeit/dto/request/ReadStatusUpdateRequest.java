package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "수정할 읽음 상태 정보")
public record ReadStatusUpdateRequest(

        @NotNull(message = "읽음 시간은 필수로 기재해야 합니다.")
        Instant newLastReadAt

) {
}
