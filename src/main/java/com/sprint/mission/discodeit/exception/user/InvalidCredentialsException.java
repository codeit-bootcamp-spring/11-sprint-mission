package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InvalidCredentialsException extends UserException {

  public InvalidCredentialsException() {
    super(ErrorCode.INVALID_USER_CREDENTIALS);
  }

  public static InvalidCredentialsException wrongPassword() {
    InvalidCredentialsException exception = new InvalidCredentialsException();
    exception.addDetail("reason", "비밀번호가 일치하지 않습니다.");
    return exception;
  }
}
