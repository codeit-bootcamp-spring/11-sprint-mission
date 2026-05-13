package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.UUID;

public class DuplicateUserStatusException extends DiscodeitException {

    public DuplicateUserStatusException() {
        super(ErrorCode.USER_STATUS_DUPLICATE);
    }

    public static DuplicateUserStatusException withUserId(UUID userId) {
        DuplicateUserStatusException exception = new DuplicateUserStatusException();
        exception.addDetail("userId", userId);
        return exception;
    }
}
