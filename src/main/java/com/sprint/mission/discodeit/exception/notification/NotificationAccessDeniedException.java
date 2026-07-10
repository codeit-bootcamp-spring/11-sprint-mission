package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationAccessDeniedException extends NotificationException {

  public NotificationAccessDeniedException(UUID notificationId, UUID requesterId) {
    super(ErrorCode.FORBIDDEN, Map.of(
        "notificationId", notificationId,
        "requesterId", requesterId
    ));
  }
}
