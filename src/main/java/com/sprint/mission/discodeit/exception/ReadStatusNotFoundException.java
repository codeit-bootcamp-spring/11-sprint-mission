package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ReadStatusNotFoundException extends RuntimeException {
    public ReadStatusNotFoundException(UUID userId, UUID channelId) {
        super("존재하지 않는 ReadStatus입니다. userId=" + userId + " channelId=" + channelId);
    }
}