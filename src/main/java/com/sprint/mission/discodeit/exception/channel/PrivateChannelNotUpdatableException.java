package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.DiscodeitException;

public class PrivateChannelNotUpdatableException extends ChannelException {
    public PrivateChannelNotUpdatableException() {
        super(ChannelErrorCode.PRIVATE_CHANNEL_NOT_UPDATABLE, null);
    }
}
