package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class DuplicateUserException extends UserException {

  private DuplicateUserException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static DuplicateUserException withUsername(String username) {
    return new DuplicateUserException(
        ErrorCode.DUPLICATE_USER,
        Map.of(
            "username", username
        )
    );
  }

  public static DuplicateUserException withEmail(String email) {
    return new DuplicateUserException(
        ErrorCode.DUPLICATE_USER,
        Map.of(
            "email", email
        )
    );
  }
}