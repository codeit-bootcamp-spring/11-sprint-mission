package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // --- Auth (인증) ---
  PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "Password Does Not Match"),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "Login Failed"),

  // --- User (사용자) ---
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found"),
  DUPLICATE_USER(HttpStatus.CONFLICT, "User Already Exists"),
  EMAIL_EXISTS(HttpStatus.CONFLICT, "Email Already Exists"),
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "User Status Not Found"),
  USER_STATUS_EXISTS(HttpStatus.CONFLICT, "User Status Already Exists"),

  // --- Channel (채널) ---
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "Channel Not Found"),
  CHANNEL_MODIFY_PRIVATE(HttpStatus.BAD_REQUEST, "Private Channel Cannot Be Modified"),

  // --- Message & Read Status (메시지 및 읽음 상태) ---
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message Not Found"),
  MESSAGE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "Message Content Is Empty"),
  READ_STATUS_EXISTS(HttpStatus.CONFLICT, "Read Status Already Exists"),
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "Read Status Not Found"),

  // --- File & Binary (파일 및 바이너라) ---
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "File Not Found"),
  FILE_EMPTY(HttpStatus.BAD_REQUEST, "File Empty"),
  BINARY_CONTENT_NOT_EXISTS(HttpStatus.NOT_FOUND, "Binary Content Not Exists"),

  // --- Global & Server (공통 및 서버) ---
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

  @Getter
  private final HttpStatus status;

  @Getter
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
