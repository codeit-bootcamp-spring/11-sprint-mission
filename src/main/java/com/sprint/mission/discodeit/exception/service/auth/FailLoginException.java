package com.sprint.mission.discodeit.exception.service.auth;

import com.sprint.mission.discodeit.exception.AuthException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class FailLoginException extends AuthException {

  public FailLoginException() {
    super(ErrorCode.FAIL_LOGIN, null);
  }
}
