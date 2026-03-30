package com.sprint.mission.discodeit.exception.readstatus;

import java.util.UUID;

public class ReadStatusOfUserAndChannelNotFoundException extends RuntimeException {
    public ReadStatusOfUserAndChannelNotFoundException(UUID userId, UUID channelId) {
        super("Readstatus not found of user: " + userId + ", channel: " + channelId);
    }
}
