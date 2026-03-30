package com.sprint.mission.discodeit.exception;

// 비밀번호 불일치
public class DiscodeitInvalidPasswordException extends DiscodeitException {

  public DiscodeitInvalidPasswordException() {
    super("아이디 비밀번호가 일치하지 않습니다.");
  }
}
