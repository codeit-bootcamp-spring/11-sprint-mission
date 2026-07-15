package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto (
    UUID id,
    UUID receiverId,
    String title,
    String content,
    Instant createdAt
) {
}
