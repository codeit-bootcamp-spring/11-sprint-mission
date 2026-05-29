package com.sprint.mission.discodeit.exception.common;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class ValidationErrorException extends DiscodeitException {

  private ValidationErrorException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static ValidationErrorException withDetails(Map<String, Object> details) {
    return new ValidationErrorException(
        ErrorCode.VALIDATION_ERROR,
        details
    );
  }
}