package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

  // Channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "프라이빗 채널은 수정할 수 없습니다."),
  INSUFFICIENT_PARTICIPANTS(HttpStatus.BAD_REQUEST, "프라이빗 채널 생성에는 최소 2명의 사용자가 필요합니다."),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),

  // BinaryContent (File)
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 파일을 찾을 수 없습니다."),
  MISSING_FILE_CONTENT(HttpStatus.BAD_REQUEST, "업로드할 파일의 내용이 비어있습니다."),

  // ReadStatus
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽음 상태 정보를 찾을 수 없습니다."),
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 채널에 이미 사용자의 읽음 상태가 존재합니다."),

  // UserStatus
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 접속 상태 정보를 찾을 수 없습니다."),
  USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자 상태 정보입니다."),

  // Auth
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");

  private final HttpStatus status; // Enum 클래스에서는 상수들을 무조건 최상단에 먼저 선언해야 한다.
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
