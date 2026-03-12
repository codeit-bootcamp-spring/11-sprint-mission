package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(UUID id) {
        super("Message not found: " + id);
    }
}
