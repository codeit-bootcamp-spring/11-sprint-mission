package com.sprint.mission.discodeit.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content
) {

}