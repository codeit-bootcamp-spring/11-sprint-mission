package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Message extends Common{
    private String contents;
    private UUID userId;
    private UUID channelId;

    public Message(String contents, UUID userId, UUID channelId) {
        super();
        this.contents = contents;
        this.userId = userId;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "contents='" + contents + '\'' +
                ", userId=" + userId +
                ", channelId=" + channelId +
                '}';
    }
}
