package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class StompAuthenticationException extends AuthenticationException {

  private final ErrorCode errorCode;

  public StompAuthenticationException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public StompAuthenticationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
  }
}
