package com.sprint.mission.discodeit.exception.channel;

import java.util.Map;
import java.util.UUID;

public class ChannelNotFoundException extends ChannelException{
    public ChannelNotFoundException(UUID channelId) {
        super(ChannelErrorCode.CHANNEL_NOT_FOUND, Map.of("searchedChannelId", channelId));
    }
}
