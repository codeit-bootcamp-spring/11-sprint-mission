package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  FAIL_LOGIN(HttpStatus.BAD_REQUEST, "유저 이름 혹은 비밀번호가 잘못되었습니다."),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.BAD_REQUEST, "이미 해당 유저가 존재합니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "다른 사람이 사용중인 이메일입니다."),

  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE(HttpStatus.BAD_REQUEST, "개인 채널은 수정할 수 없습니다."),

  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메시지를 찾을 수 없습니다."),

  DUPLICATE_USERSTATUS(HttpStatus.CONFLICT, "이미 존재하는 유저 상태입니다."),
  USERSTATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 상태를 찾을 수 없습니다."),

  DUPLICATE_READSTATUS(HttpStatus.CONFLICT, "이미 존재하는 읽음 상태입니다."),
  READSTATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 읽음 상태를 찾을 수 없습니다."),

  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 파일을 찾을 수 없습니다."),
  FILE_PUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),

  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),

  REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

  ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),

  DUMMY_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "더미 예외입니다.");

  private final HttpStatus status;
  private final String message;


  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
