package com.sprint.mission.discodeit.exception.service.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.NotificationException;
import java.util.Map;
import java.util.UUID;

public class NonExistNotificationException extends NotificationException {

  public NonExistNotificationException(UUID notificationId) {

    super(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("notificationId", notificationId + ""));
  }


}
