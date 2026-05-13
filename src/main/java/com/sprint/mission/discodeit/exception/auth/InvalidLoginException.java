package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidLoginException extends AuthException {

  public InvalidLoginException(String username) {
    super(ErrorCode.INVALID_LOGIN, Map.of("username", username));
  }
}
