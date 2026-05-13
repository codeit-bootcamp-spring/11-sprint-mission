package com.sprint.mission.discodeit.dto.readstatusdto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReadStatusUpdateRequest(
    @NotNull(message = "최근 읽은 시간은 null이 될 수 없습니다.")
    Instant newLastReadAt

) {

}
