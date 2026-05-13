package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class DuplicateUsernameException extends DiscodeitException {

    public DuplicateUsernameException() {
        super(ErrorCode.USER_USERNAME_DUPLICATE);
    }
}