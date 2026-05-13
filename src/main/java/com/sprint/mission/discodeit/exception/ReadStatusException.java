package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class ReadStatusException extends DiscodeitException {

  public ReadStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
