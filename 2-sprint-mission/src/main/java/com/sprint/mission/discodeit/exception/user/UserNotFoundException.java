package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.List;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  private UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public static UserNotFoundException withId(UUID userId) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail(ErrorKey.USER_ID, userId);
    return exception;
  }

  public static UserNotFoundException withUsername(String username) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail(ErrorKey.USERNAME, username);
    return exception;
  }

  public static UserNotFoundException withIds(List<UUID> userIds) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail(ErrorKey.USER_ID, userIds);
    return exception;
  }
}

