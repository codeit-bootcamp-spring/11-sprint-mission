package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorCode {
  USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(400, "이미 존재하는 사용자입니다."),
      CHANNEL_NOT_FOUND(404, "채널을 찾을 수 없습니다."),
        INVALID_INPUT_VALUE(400, "잘못된 입력값입니다.");

  private final int status;
  private final String message;

}
