package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // User
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  USER_ALREADY_EXISTS("이미 존재하는 사용자입니다."),

  // Channel
  CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
  CHANNEL_UPDATE_NOT_ALLOWED("Private 채널은 수정할 수 없습니다."),

  // Message
  MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다."),

  // BinaryContent
  BINARY_CONTENT_NOT_FOUND("파일을 찾을 수 없습니다."),

  // Auth
  INVALID_PASSWORD("비밀번호가 올바르지 않습니다."),

  // Common
  INVALID_REQUEST("잘못된 요청입니다.");

  private final String message;
}