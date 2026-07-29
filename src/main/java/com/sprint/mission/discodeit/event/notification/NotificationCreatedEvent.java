package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto data
) {

}
