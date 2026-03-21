package com.sprint.mission.discodeit.exception.userstatus;

import java.util.UUID;

public class UserStatusNotFoundException extends RuntimeException {
    public UserStatusNotFoundException(UUID id) {
        super("UserStatus not found: " + id);
    }
}
