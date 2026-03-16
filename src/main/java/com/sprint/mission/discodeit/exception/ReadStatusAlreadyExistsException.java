package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ReadStatusAlreadyExistsException extends RuntimeException {
    public ReadStatusAlreadyExistsException(UUID userId, UUID channelId) {
        super("ReadStatus already exists for user: " + userId + " in channel: " + channelId);
    }
}
