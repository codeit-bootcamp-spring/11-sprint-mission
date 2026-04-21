package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

  private final ERROR error;

  public enum ERROR {
    // Auth
    AUTH_INVALID_CREDENTIALS("AUTH_001", "INVALID_CREDENTIALS", "password is not matched.",
        HttpStatus.UNAUTHORIZED),

    // User
    USER_USERNAME_DUPLICATED("USER_001", "DUPLICATE_RESOURCE", "username cannot be duplicated.",
        HttpStatus.CONFLICT),
    USER_EMAIL_DUPLICATED("USER_002", "DUPLICATE_RESOURCE", "email cannot be duplicated.",
        HttpStatus.CONFLICT),
    USER_NOT_FOUND("USER_003", "ENTITY_NOT_FOUND", "requested user not found.",
        HttpStatus.NOT_FOUND),

    // Channel
    CHANNEL_NAME_DUPLICATED("CHANNEL_001", "DUPLICATE_RESOURCE", "name cannot be duplicated.",
        HttpStatus.CONFLICT),
    CHANNEL_NO_VALID_PARTICIPANTS("CHANNEL_002", "VALIDATION_ERROR", "no valid participants found.",
        HttpStatus.BAD_REQUEST),
    CHANNEL_NOT_FOUND("CHANNEL_003", "ENTITY_NOT_FOUND", "requested channel not found.",
        HttpStatus.NOT_FOUND),
    CHANNEL_PRIVATE_UPDATE_FORBIDDEN("CHANNEL_004", "INVALID_OPERATION",
        "private channel cannot be updated.", HttpStatus.UNPROCESSABLE_ENTITY),

    // Message
    MESSAGE_CHANNEL_ACCESS_REQUIRED("MESSAGE_001", "INVALID_OPERATION",
        "sender cannot send message without channel participation.",
        HttpStatus.UNPROCESSABLE_ENTITY),
    MESSAGE_NOT_FOUND("MESSAGE_002", "ENTITY_NOT_FOUND", "requested message not found.",
        HttpStatus.NOT_FOUND),

    // ReadStatus
    READ_STATUS_DUPLICATED("READ_STATUS_001", "DUPLICATE_RESOURCE",
        "read status already exists for the same user and channel.", HttpStatus.CONFLICT),
    READ_STATUS_NOT_FOUND("READ_STATUS_002", "ENTITY_NOT_FOUND", "requested read status not found.",
        HttpStatus.NOT_FOUND),

    // UserStatus
    USER_STATUS_DUPLICATED("USER_STATUS_001", "DUPLICATE_RESOURCE",
        "user status already exists for the same user.", HttpStatus.CONFLICT),
    USER_STATUS_NOT_FOUND("USER_STATUS_002", "ENTITY_NOT_FOUND", "requested user status not found.",
        HttpStatus.NOT_FOUND),

    // BinaryContent
    BINARY_CONTENT_NOT_FOUND("BINARY_CONTENT_001", "ENTITY_NOT_FOUND",
        "requested binary content not found.", HttpStatus.NOT_FOUND),

    // Common
    COMMON_VALIDATION_ERROR("COMMON_001", "VALIDATION_ERROR", "request validation failed.",
        HttpStatus.BAD_REQUEST),
    COMMON_UNEXPECTED_ERROR("COMMON_002", "INTERNAL_SERVER_ERROR", "unexpected error occurred.",
        HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String exceptionType;
    private final String message;
    private final HttpStatus httpStatus;

    ERROR(String code, String exceptionType, String message, HttpStatus httpStatus) {
      this.code = code;
      this.exceptionType = exceptionType;
      this.message = message;
      this.httpStatus = httpStatus;
    }

    public String getCode() {
      return this.code;
    }

    public String getExceptionType() {
      return this.exceptionType;
    }

    public String getMessage() {
      return this.message;
    }

    public HttpStatus getHttpStatus() {
      return this.httpStatus;
    }
  }

  public ApiException(ERROR error) {
    super(error.getMessage());
    this.error = error;
  }

  public ERROR getError() {
    return this.error;
  }
}
