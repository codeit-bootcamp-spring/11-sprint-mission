package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(UUID id) {
        super("존재하지 않는 채널입니다. id=" + id);
    }
}
