package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class PasswordMismatchException extends DiscodeitException {

  public PasswordMismatchException(String username) {
    super(ErrorCode.PASSWORD_MISMATCH, Map.of("username", username));
  }
}
