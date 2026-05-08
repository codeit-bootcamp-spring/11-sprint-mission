package com.sprint.mission.discodeit.exception.message;

import java.util.Map;
import java.util.UUID;

public class MessageNotFoundException extends MessageException {
    public MessageNotFoundException(UUID messageId) {
        super(MessageErrorCode.MESSAGE_NOT_FOUND, Map.of("searchedMessageId", messageId));
    }
}
