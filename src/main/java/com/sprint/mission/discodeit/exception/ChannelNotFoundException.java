package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ChannelNotFoundException extends RuntimeException {

    public ChannelNotFoundException(UUID id) {
        super("Channel not found: " + id);
    }
}
