package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class UserException extends DiscodeitException {
  public UserException(ErrorCode errorCode, Object details) {
    super(errorCode, details);
  }

}

public class UserNotFoundException extends UserException {
  public UserNotFoundException(Object details) {
    super(ErrorCode.USER_NOT_FOUND, details);
  }
}
