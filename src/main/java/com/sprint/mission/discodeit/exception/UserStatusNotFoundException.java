package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class UserStatusNotFoundException extends RuntimeException {
    public UserStatusNotFoundException(UUID userId) {
        super("존재하지 않는 UserStatus입니다. userId=" + userId);
    }
}