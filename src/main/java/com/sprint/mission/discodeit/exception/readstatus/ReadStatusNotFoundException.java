package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class ReadStatusNotFoundException extends DiscodeitException {

    public ReadStatusNotFoundException() {
        super(ErrorCode.READ_STATUS_NOT_FOUND);
    }
}