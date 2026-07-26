package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.notification.NotificationResponse;

public record NotificationCreatedEvent(
    NotificationResponse notification
) {

  public static final String TOPIC = "discodeit.NotificationCreatedEvent";
}