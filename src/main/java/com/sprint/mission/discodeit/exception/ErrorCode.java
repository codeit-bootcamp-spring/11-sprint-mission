package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // Auth
  INVALID_USER_DETAILS("AUTH_001", "user details is invalid.", HttpStatus.UNAUTHORIZED),
  INVALID_CREDENTIALS("AUTH_002", "password is not matched.", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED("AUTH_003", "authentication is required.", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("AUTH_004", "access is denied.", HttpStatus.FORBIDDEN),
  INVALID_TOKEN("AUTH_005", "token is invalid or expired.", HttpStatus.UNAUTHORIZED),
  INVALID_REFRESH_TOKEN("AUTH_006", "refresh token is invalid or expired.", HttpStatus.UNAUTHORIZED),

  // User
  USER_NOT_FOUND("USER_001", "requested user not found.", HttpStatus.NOT_FOUND),
  DUPLICATE_USER("USER_002", "user already exists with request fields.", HttpStatus.CONFLICT),

  // Channel
  CHANNEL_NOT_FOUND("CHANNEL_001", "requested channel not found.", HttpStatus.NOT_FOUND),
  DUPLICATE_CHANNEL("CHANNEL_002", "channel already exists with request fields.",
      HttpStatus.CONFLICT),
  NO_VALID_PARTICIPANTS("CHANNEL_003", "no valid participants found.", HttpStatus.BAD_REQUEST),
  PRIVATE_CHANNEL_UPDATE_FORBIDDEN("CHANNEL_004", "private channel cannot be updated.",
      HttpStatus.UNPROCESSABLE_ENTITY),

  // Message
  MESSAGE_NOT_FOUND("MESSAGE_001", "requested message not found.", HttpStatus.NOT_FOUND),
  MESSAGE_WITHOUT_CHANNEL_ACCESS("MESSAGE_002",
      "sender cannot send message without channel participation.", HttpStatus.UNPROCESSABLE_ENTITY),

  // ReadStatus
  READ_STATUS_NOT_FOUND("READ_STATUS_001", "requested read status not found.",
      HttpStatus.NOT_FOUND),
  DUPLICATE_READ_STATUS("READ_STATUS_002",
      "read status already exists with request fields.", HttpStatus.CONFLICT),

  // BinaryContent
  BINARY_CONTENT_NOT_FOUND("BINARY_CONTENT_001", "requested binary content not found.",
      HttpStatus.NOT_FOUND),

  // Common
  UNEXPECTED_ERROR("COMMON_001", "unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
  VALIDATION_ERROR("COMMON_002", "request validation failed.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  public String getCode() {
    return this.code;
  }

  public String getMessage() {
    return this.message;
  }

  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }
}