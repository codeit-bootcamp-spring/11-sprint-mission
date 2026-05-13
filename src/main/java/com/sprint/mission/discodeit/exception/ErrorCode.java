package com.sprint.mission.discodeit.exception;

public enum ErrorCode {

  // USER
  USER_NOT_FOUND("해당 유저가 존재하지 않습니다."),
  DUPLICATE_USERNAME("이미 존재하는 이름입니다."),
  DUPLICATE_EMAIL("이미 존재하는 이메일입니다."),

  // CHANNEL
  CHANNEL_NOT_FOUND("해당 채널이 존재하지 않습니다."),
  PRIVATE_CHANNEL_UPDATE("PRIVATE 채널은 수정할 수 없습니다."),

  // MESSAGE
  MESSAGE_NOT_FOUND("해당 메시지가 존재하지 않습니다."),

  // USER_STATUS
  USER_STATUS_NOT_FOUND("해당 UserStatus가 존재하지 않습니다."),
  USER_STATUS_ALREADY_EXISTS("이미 존재하는 UserStatus입니다."),

  // READ_STATUS
  READ_STATUS_NOT_FOUND("해당 ReadStatus가 존재하지 않습니다."),
  READ_STATUS_ALREADY_EXISTS("이미 존재하는 ReadStatus입니다."),

  // BINARY_CONTENT
  BINARY_CONTENT_NOT_FOUND("해당 BinaryContent가 존재하지 않습니다."),
  ATTACHMENT_SAVE_FAILED("첨부파일 저장에 실패하였습니다."),

  // AUTH
  INVALID_LOGIN("로그인 정보가 올바르지 않습니다."),

  // 공통
  INVALID_REQUEST("잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}