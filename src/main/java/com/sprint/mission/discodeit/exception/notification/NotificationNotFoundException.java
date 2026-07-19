package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  private NotificationNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static NotificationNotFoundException withId(UUID notificationId) {
    return new NotificationNotFoundException(
        ErrorCode.NOTIFICATION_NOT_FOUND,
        Map.of(
            "notificationId", notificationId
        )
    );
  }
}