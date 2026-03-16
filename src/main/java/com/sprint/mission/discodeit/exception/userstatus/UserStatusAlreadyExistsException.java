package com.sprint.mission.discodeit.exception.userstatus;

import java.util.UUID;

public class UserStatusAlreadyExistsException extends RuntimeException {
    public UserStatusAlreadyExistsException(UUID userId) {
        super("UserStatus already exists for user: " + userId);
    }
}
