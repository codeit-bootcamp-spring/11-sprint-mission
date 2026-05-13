package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class UserAlreadyExistsException extends UserException {

  public UserAlreadyExistsException(String field, String value) {
    super(ErrorCode.USER_ALREADY_EXISTS, Map.of(field, value));
  }
}