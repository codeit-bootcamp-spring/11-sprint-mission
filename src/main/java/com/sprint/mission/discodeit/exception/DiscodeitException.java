package com.sprint.mission.discodeit.exception;

import lombok.Getter;


@Getter
public class DiscodeitException extends RuntimeException {
  private final ErrorCode errorCode;
  private final Object details;

  public DiscodeitException(ErrorCode errorCode, Object details) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details;
  }

}
