package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InvalidCredentialsException extends UserException {

  private InvalidCredentialsException() {
    super(ErrorCode.INVALID_CREDENTIALS);
  }

  public static InvalidCredentialsException wrongPassword() {
    return new InvalidCredentialsException();
  }
}