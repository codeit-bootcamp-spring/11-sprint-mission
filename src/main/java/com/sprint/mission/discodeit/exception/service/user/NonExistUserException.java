package com.sprint.mission.discodeit.exception.service.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserException;
import java.util.Map;
import java.util.UUID;

public class NonExistUserException extends UserException {

  public NonExistUserException(String key, String value) {
    super(ErrorCode.USER_NOT_FOUND, Map.of(key, value));
  }

  public NonExistUserException(UUID userId) {
    super(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
  }

  public static NonExistUserException ByEmail(String email) {
    return new NonExistUserException("email", email);
  }

  public static NonExistUserException ByUsername(String username) {

    return new NonExistUserException("username", username);
  }

}
