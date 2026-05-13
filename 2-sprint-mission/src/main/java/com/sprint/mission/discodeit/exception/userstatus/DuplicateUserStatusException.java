package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class DuplicateUserStatusException extends UserStatusException {

  private DuplicateUserStatusException() {
    super(ErrorCode.DUPLICATE_USER_STATUS);
  }

  public static DuplicateUserStatusException withUserId(UUID userId) {
    DuplicateUserStatusException exception = new DuplicateUserStatusException();
    exception.addDetail(ErrorKey.USER_ID, userId);
    return exception;
  }
}