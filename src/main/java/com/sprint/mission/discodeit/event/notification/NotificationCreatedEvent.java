package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.event.CreatedEvent;
import java.time.Instant;

public class NotificationCreatedEvent extends CreatedEvent<NotificationDto> {

  public NotificationCreatedEvent(NotificationDto data, Instant createdAt) {
    super(data, createdAt);
  }
}
