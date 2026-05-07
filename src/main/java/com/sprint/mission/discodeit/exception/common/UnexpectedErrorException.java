package com.sprint.mission.discodeit.exception.common;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UnexpectedErrorException extends DiscodeitException {

  private UnexpectedErrorException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UnexpectedErrorException withCause() {
    return new UnexpectedErrorException(
        ErrorCode.UNEXPECTED_ERROR,
        null
    );
  }
}