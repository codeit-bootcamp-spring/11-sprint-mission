package com.sprint.mission.discodeit.exception.channel;


//존재하지 않는 ID로 채널을 조회, 수정, 삭제하려 할 때 404
public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(String message) {
        super(message);
    }
}
