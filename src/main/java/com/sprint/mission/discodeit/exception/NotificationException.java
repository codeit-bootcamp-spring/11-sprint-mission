package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class NotificationException extends DiscodeitException {

  public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
