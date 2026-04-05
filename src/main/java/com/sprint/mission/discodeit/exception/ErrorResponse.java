package com.sprint.mission.discodeit.exception;

public record ErrorResponse(
    String code,
    String exceptionType,
    String message
) {

  public static ErrorResponse from(ApiException.ERROR errorCode) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getExceptionType(),
        errorCode.getMessage()
    );
  }
}

