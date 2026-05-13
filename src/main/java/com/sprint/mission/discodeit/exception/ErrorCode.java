package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  //user
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

  //channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "PRIVATE 채널을 수정할 수 없습니다."),

  //message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),

  //auth
  PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

  //binarycontent
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

  //readstatus
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽음 상태 정보를 찾을 수 없습니다."),
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 채널의 읽음 상태 정보가 존재합니다."),

  //userstatus
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 상태 정보를 찾을 수 없습니다."),
  USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 사용자의 상태 정보가 존재합니다.");

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
