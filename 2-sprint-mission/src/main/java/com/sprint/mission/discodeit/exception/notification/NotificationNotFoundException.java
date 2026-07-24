package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  private NotificationNotFoundException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND);
  }

  public static NotificationNotFoundException withId(UUID notificationId) {
    NotificationNotFoundException exception = new NotificationNotFoundException();
    exception.addDetail(ErrorKey.NOTIFICATION_ID, notificationId);
    return exception;
  }
}