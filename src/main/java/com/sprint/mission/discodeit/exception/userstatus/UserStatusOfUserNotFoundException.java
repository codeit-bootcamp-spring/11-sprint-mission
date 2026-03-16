package com.sprint.mission.discodeit.exception.userstatus;

import java.util.UUID;

public class UserStatusOfUserNotFoundException extends RuntimeException {
    public UserStatusOfUserNotFoundException(UUID id) {
        super("UserStatus not found of User " + id);
    }
}
