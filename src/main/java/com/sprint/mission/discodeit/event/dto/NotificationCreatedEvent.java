package com.sprint.mission.discodeit.event.dto;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import java.time.Instant;

public record NotificationCreatedEvent(
    NotificationDto data,
    Instant createdAt
) {

}
