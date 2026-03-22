package com.sprint.mission.discodeit.exception.channel;


//비공개 채널을 수정하려 하거나, 허용되지 않는 동작을 할 때 400
public class ChannelOperationException extends RuntimeException {
    public ChannelOperationException(String message) {
        super(message);
    }
}
