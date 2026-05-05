package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

  // Channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "PRIVATE 채널은 수정할 수 없습니다."),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}