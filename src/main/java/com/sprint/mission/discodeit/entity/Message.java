package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends Common{
    private String contents;
    private UUID userId;
    private UUID channelId;
    private List<UUID> attachmentIds;

    public Message(String contents, UUID userId, UUID channelId) {
        super();
        this.contents = contents;
        this.userId = userId;
        this.channelId = channelId;
        this.attachmentIds = new ArrayList<>();
    }

    public Message(String contents, UUID userId, UUID channelId, List<UUID> attachmentIds) {
        super();
        this.contents = contents;
        this.userId = userId;
        this.channelId = channelId;
        this.attachmentIds = new ArrayList<>(attachmentIds);
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }

    public void setAttachmentIds(List<UUID> attachmentIds) {
        this.attachmentIds = new ArrayList<>(attachmentIds);
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
