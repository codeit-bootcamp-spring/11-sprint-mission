package com.sprint.mission.discodeit.exception.userstatus;

import java.util.Map;
import java.util.UUID;

public class UserStatusAlreadyExistsException extends UserStatusException{
    public UserStatusAlreadyExistsException(UUID userId) {
        super(UserStatusErrorCode.USER_STATUS_ALREADY_EXISTS, Map.of("userId", userId));
    }
}
