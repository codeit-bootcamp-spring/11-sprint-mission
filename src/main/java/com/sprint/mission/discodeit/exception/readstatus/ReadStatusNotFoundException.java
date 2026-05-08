package com.sprint.mission.discodeit.exception.readstatus;

import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ReadStatusException{
    public ReadStatusNotFoundException(UUID readStatusId) {
        super(ReadStatusErrorCode.READ_STATUS_NOT_FOUND, Map.of("searchedReadStatusId", readStatusId));
    }
}
