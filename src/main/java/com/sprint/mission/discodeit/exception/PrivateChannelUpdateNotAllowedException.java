package com.sprint.mission.discodeit.exception;

public class PrivateChannelUpdateNotAllowedException extends RuntimeException {
    public PrivateChannelUpdateNotAllowedException() {
        super("PRIVATE channel cannot be updated");
    }
}
