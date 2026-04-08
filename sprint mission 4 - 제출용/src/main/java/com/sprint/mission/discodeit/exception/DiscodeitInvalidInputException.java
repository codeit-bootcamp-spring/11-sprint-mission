package com.sprint.mission.discodeit.exception;

public class DiscodeitInvalidInputException extends DiscodeitException {

  public DiscodeitInvalidInputException(String message) {
    super(message);
  }

  public static DiscodeitInvalidInputException blankField(String fieldName) {
    return new DiscodeitInvalidInputException(fieldName + "이(가) null이거나 blank입니다.");
  }
}
