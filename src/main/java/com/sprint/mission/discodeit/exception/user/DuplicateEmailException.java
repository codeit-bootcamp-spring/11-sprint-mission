package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class DuplicateEmailException extends DiscodeitException {

    public DuplicateEmailException() {
        super(ErrorCode.USER_EMAIL_DUPLICATE);
    }
}