package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class MessageNotFoundException extends DiscodeitException {

    public MessageNotFoundException() {
        super(ErrorCode.MESSAGE_NOT_FOUND);
    }
}