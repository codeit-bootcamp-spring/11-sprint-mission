package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    // Notification ID
    UUID id,

    // 만들어진 시각
    Instant createdAt,

    // 수신자 ID(User Id, 받는사람)
    UUID receiverId,

    // 알림 제목
    String title,

    // 알림 내용
    String content
) {

}
