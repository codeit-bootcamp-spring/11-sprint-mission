package com.sprint.mission.discodeit.dto.readstatus;

import java.time.Instant;

// 읽음 상태 업데이트
public record ReadStatusUpdateRequest(
    Instant newLastReadAt
) {

}
