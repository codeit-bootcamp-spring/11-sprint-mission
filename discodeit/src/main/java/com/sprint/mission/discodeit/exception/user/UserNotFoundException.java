package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  private UserNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UserNotFoundException withId(UUID userId) {
    return new UserNotFoundException(
        ErrorCode.USER_NOT_FOUND,
        Map.of(
            "userId", userId
        )
    );
  }

  public static UserNotFoundException withUsername(String username) {
    return new UserNotFoundException(
        ErrorCode.USER_NOT_FOUND,
        Map.of(
            "username", username
        )
    );
  }
}