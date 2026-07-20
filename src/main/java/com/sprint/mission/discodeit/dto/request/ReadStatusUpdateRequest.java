package com.sprint.mission.discodeit.dto.request;

import java.time.Instant;

public record ReadStatusUpdateRequest(
    Instant newLastReadAt,

    // 다시 읽었을 때 newLastReadAt 따로, 알림 On/Off 따로
    // Put이 아닌 Patch므로 null 가능
    Boolean newNotificationEnabled
) {

}
