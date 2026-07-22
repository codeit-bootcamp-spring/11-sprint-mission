package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    UUID receiveId,
    String title,
    String content

) {


}
