package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;

public class UserAlreadyExistsException extends UserException {

  private UserAlreadyExistsException() {
    super(ErrorCode.DUPLICATE_USER);
  }

  public static UserAlreadyExistsException withEmail(String email) {
    UserAlreadyExistsException exception = new UserAlreadyExistsException();
    exception.addDetail(ErrorKey.EMAIL, email);
    return exception;
  }

  public static UserAlreadyExistsException withUsername(String username) {
    UserAlreadyExistsException exception = new UserAlreadyExistsException();
    exception.addDetail(ErrorKey.USERNAME, username);
    return exception;
  }
}