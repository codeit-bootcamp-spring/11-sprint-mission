package com.sprint.mission.discodeit.exception.channel;


//방장이 아닌데 채널을 수정/삭제하려 할 때 403
public class NotChannelAdminException extends RuntimeException{
    public NotChannelAdminException(String message) {
        super(message);
    }
}
