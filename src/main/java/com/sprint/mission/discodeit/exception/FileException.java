package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class FileException extends DiscodeitException {

  public FileException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
