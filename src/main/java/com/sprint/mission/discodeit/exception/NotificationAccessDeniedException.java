package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public class NotificationAccessDeniedException extends NotificationException {

  public NotificationAccessDeniedException(UUID notificationId, UUID requesterId) {
    super(ErrorCode.NOTIFICATION_ACCESS_DENIED,
        Map.of("notificationId", notificationId, "requesterId", requesterId));
  }
}
