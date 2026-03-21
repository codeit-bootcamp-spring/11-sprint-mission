package com.sprint.mission.discodeit.exception.channel;

public class PrivateChannelUpdateNotAllowedException extends RuntimeException {
    public PrivateChannelUpdateNotAllowedException() {
        super("PRIVATE channel cannot be updated");
    }
}
