package com.sprint.mission.discodeit.exception.userstatus;

import java.util.Map;
import java.util.UUID;

public class UserStatusNotFoundException extends UserStatusException{
    private UserStatusNotFoundException(Map<String, Object> details) {
        super(UserStatusErrorCode.USER_STATUS_NOT_FOUND, details);
    }

    public static UserStatusNotFoundException byUserStatusId(UUID userStatusId) {
        return new UserStatusNotFoundException(Map.of("searchedUserStatusId", userStatusId));
    }

    public static UserStatusNotFoundException byUserId(UUID userId) {
        return new UserStatusNotFoundException(Map.of("searchedUserId", userId));
    }
}
