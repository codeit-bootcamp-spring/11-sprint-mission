package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.baseentity.BaseUpdatableEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseUpdatableEntity {
    private String contents;
    private final UUID userId;
    private final UUID channelId;
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
