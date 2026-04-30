package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.UserException;

import java.util.Map;
import java.util.UUID;

public class UserStatusNotFoundException extends UserException {
    public UserStatusNotFoundException(UUID userStatusId) {
        super(ErrorCode.USER_STATUS_NOT_FOUND, Map.of("userStatusId", userStatusId));
    }

    public UserStatusNotFoundException(User user) {
        super(ErrorCode.USER_STATUS_NOT_FOUND, Map.of("userId", user.getId()));
    }
}
